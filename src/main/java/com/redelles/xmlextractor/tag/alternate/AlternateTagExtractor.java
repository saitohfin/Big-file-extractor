package com.redelles.xmlextractor.tag.alternate;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

public interface AlternateTagExtractor {

    String key();

    boolean isCompatible(XMLEvent event);

    String extract(
        XMLEvent event,
        XMLEventReader reader
    );

    void decreaseTagCount();

    boolean canContinueProcessing();

    AlternateTagExtractor rebuild();

}
