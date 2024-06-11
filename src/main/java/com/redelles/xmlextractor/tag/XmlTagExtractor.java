package com.redelles.xmlextractor.tag;

import com.ctc.wstx.exc.WstxEOFException;
import com.ctc.wstx.exc.WstxParsingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import com.redelles.xmlextractor.XmlTagProcessor;
import com.redelles.xmlextractor.tag.alternate.AlternateTagExtractor;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class XmlTagExtractor {

    public static final int ERROR_LIMIT = 100;
    private final Collection<AlternateTagExtractor> specialExtractors;
    private final Collection<XmlTagPath> tagPaths;
    private final Collection<String> stopTags;

    public XmlTagExtractor(
        final List<XmlTagPath> tagPaths,
        final List<String> stopTags,
        final Collection<AlternateTagExtractor> specialExtractors
    ) {
        this.tagPaths = new ArrayList<>(tagPaths);
        this.stopTags = new ArrayList<>(stopTags);
        this.specialExtractors = new ArrayList<>(specialExtractors);
    }

    public Map<String, Collection<String>> extract(
        final String filename,
        final InputStream inputStream
    ) {
        final Map<String, Collection<String>> result = initializeResult();
        final List<XmlTagProcessor> tagsProcessors = initializeProcessor();
        final List<AlternateTagExtractor> specialProcessors = this.specialExtractors.stream()
            .map(AlternateTagExtractor::rebuild)
            .collect(Collectors.toList());

        try (inputStream) {
            final XMLEventReader eventReader = initializeXmlReader(inputStream);
            boolean shouldStop = false;
            int exceptionsCount = 0;
            while (eventReader.hasNext() && !shouldStop) {
                try {
                    final XMLEvent event = eventReader.nextEvent();

                    if (event.isStartElement()) {
                        nextTagIfFound(tagsProcessors, event);
                    } else if (event.isEndElement()) {
                        final String elementName = previousTagIfParentClosed(tagsProcessors, event);
                        shouldStop = stopReadingIfFoundStopTag(elementName);
                    } else if (event.isCharacters()) {
                        final String text = event.asCharacters()
                            .getData();
                        extractTextWithTagsProcessors(result, tagsProcessors, text);
                    }

                    executeSpecialExtractorsIfAreCompatible(result, specialProcessors, eventReader, event);

                    shouldStop = shouldStop || (tagsProcessors.isEmpty() && this.specialExtractors.isEmpty());
                    exceptionsCount = 0;
                } catch (final WstxEOFException e) {
                    log.warn(e.getMessage());
                    shouldStop = true;
                } catch (final WstxParsingException e) {
                    log.warn(e.getMessage());
                    exceptionsCount++;
                    tagsProcessors.stream()
                        .filter(XmlTagProcessor::isReadyToWrite)
                        .forEach(XmlTagProcessor::decreaseTagCount);
                    if (exceptionsCount >= ERROR_LIMIT) {
                        shouldStop = true;
                        throw new XMLStreamException("Surpassed the limit of %s errors".formatted(ERROR_LIMIT));
                    }
                }
            }

            eventReader.close();
        } catch (final XMLStreamException | IOException e) {
            log.warn("Error extracting values from the document {} with error: {}", filename, e.getMessage());
        }
        return result;
    }

    private static void executeSpecialExtractorsIfAreCompatible(
        final Map<String, Collection<String>> result,
        final List<AlternateTagExtractor> specialProcessors,
        final XMLEventReader eventReader,
        final XMLEvent event
    ) {
        specialProcessors.stream()
            .filter(extractor -> extractor.isCompatible(event))
            .toList()
            .forEach(extractor -> {
                final String extract = extractor.extract(event, eventReader);
                if (StringUtils.isNotBlank(extract)) {
                    keepTextInMap(result, extractor.key(), extract);
                    extractor.decreaseTagCount();
                    if (!extractor.canContinueProcessing()) {
                        specialProcessors.remove(extractor);
                    }
                }
            });
    }

    private static void extractTextWithTagsProcessors(
        final Map<String, Collection<String>> result,
        final List<XmlTagProcessor> tagsProcessors,
        final String text
    ) {
        tagsProcessors.stream()
            .filter(XmlTagProcessor::isReadyToWrite)
            .toList()
            .forEach(tag -> {
                keepTextInMap(result, tag.tagPath.outKey, text.trim());
                tag.decreaseTagCount();
                if (!tag.canContinueProcessing()) {
                    tagsProcessors.remove(tag);
                }
            });
    }

    private static void keepTextInMap(
        final Map<String, Collection<String>> result,
        final String tagPath,
        final String text
    ) {
        result.computeIfPresent(tagPath, (key, collection) -> {
            collection.add(text);
            return collection;
        });
    }

    private static String previousTagIfParentClosed(
        final List<XmlTagProcessor> tagsProcessors,
        final XMLEvent event
    ) {
        final String elementName = event.asEndElement()
            .getName()
            .getLocalPart();

        tagsProcessors.stream()
            .filter(tagProcessor -> tagProcessor.getPreviousTag() != null)
            .filter(tagProcessor -> tagProcessor.getPreviousTag()
                .equalsIgnoreCase(elementName))
            .forEach(XmlTagProcessor::previous);
        return elementName;
    }

    private static void nextTagIfFound(
        final List<XmlTagProcessor> tagsProcessors,
        final XMLEvent event
    ) {
        final String elementName = event.asStartElement()
            .getName()
            .getLocalPart();

        tagsProcessors.stream()
            .filter(tagProcessor -> tagProcessor.getCurrentTag()
                .equalsIgnoreCase(elementName))
            .forEach(XmlTagProcessor::next);
    }

    private boolean stopReadingIfFoundStopTag(final String elementName) {
        return this.stopTags.contains(elementName);
    }

    private XMLEventReader initializeXmlReader(final InputStream inputStream) throws XMLStreamException, FileNotFoundException {
        final XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_VALIDATING, false);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);

        return factory.createXMLEventReader(inputStream);
    }

    private List<XmlTagProcessor> initializeProcessor() {
        return new ArrayList<>(this.tagPaths.stream()
            .map(XmlTagProcessor::new)
            .toList());
    }

    private Map<String, Collection<String>> initializeResult() {
        final Map<String, Collection<String>> map = new HashMap<>();
        this.tagPaths.forEach(tagPath -> map.put(tagPath.outKey, new ArrayList<>()));
        this.specialExtractors.forEach(specialExtractor -> map.put(specialExtractor.key(), new ArrayList<>()));
        return map;
    }

}
