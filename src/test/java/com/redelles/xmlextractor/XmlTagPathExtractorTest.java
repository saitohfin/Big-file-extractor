package com.redelles.xmlextractor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.redelles.xmlextractor.tag.XmlTagExtractor;
import com.redelles.xmlextractor.tag.XmlTagPath;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class XmlTagPathExtractorTest {

    @Test
    void whenDocumentHasTheValue_Is_Extracted_In_A_Map_With_The_Same_Name_Of_The_Tag() {
        final List<XmlTagPath> tagsToExtract = List.of(new XmlTagPath("MsgId"));

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, Collections.emptyList(), Collections.emptyList());
        final String content = "<MsgId>somevalue</MsgId>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of("MsgId", List.of("somevalue"));
        Assertions.assertEquals(expected, result);
    }

    /*
    @Test
    void whenDocumentHasSpecialVersionExtractor_And_Other_Extractor_Are_Extracted_Both() {
        final List<XmlTagPath> tagsToExtract = List.of(new XmlTagPath("MsgId"));
        final List<String> tagsToClose = List.of("DrctDbtTxInf");
        final List<AlternateTagExtractor> specialExtractors = List.of(new VersionTagExtractor());

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, tagsToClose, specialExtractors);
        final String content =
            "<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\">" +
                "<DrctDbtTxInf><MsgId>somevalue</MsgId></DrctDbtTxInf>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            "version", List.of("pain.001.001.03"),
            "MsgId", List.of("somevalue")
        );
        Assertions.assertEquals(expected, result);
    }
    */

    @Test
    void whenValueToExtract_IsInDifferentLines_TakesValuesTrim() {
        final List<XmlTagPath> tagsToExtract = List.of(new XmlTagPath("MsgId"));

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, Collections.emptyList(), Collections.emptyList());
        final String content = "<Document>" +
            "<MsgId>\nsomevalue\n</MsgId></Document>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            "MsgId", List.of("somevalue")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenDocumentContainsASameXmlTagInDifferentPlaces_OnlyIsTakingTheValueWhichContainsAllThePath() {
        final String outKey = "<father><Id>";
        final List<XmlTagPath> tagsToExtract = List.of(new XmlTagPath(List.of(
                "father",
                "Id"
            ), outKey)
        );

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, Collections.emptyList(), Collections.emptyList());
        final String content =
            "<Document><Id>somevalue</Id><father><Id>secondvalue</Id></Document>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            outKey, List.of("secondvalue")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenDocumentHasBreakLines_WouldBeTaken_TheValue_From_The_Path_And_Trim() {
        final String outKey = "<father><Id>";
        final List<XmlTagPath> tagsToExtract = List.of(new XmlTagPath(List.of(
                "father",
                "Id"
            ), outKey)
        );

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, Collections.emptyList(), Collections.emptyList());
        final String content = "<Document>" +
            "<Id>somevalue</Id><father>\n<Id>\n  secondvalue\n  </Id></father></Document>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            outKey, List.of("secondvalue")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenDocumentMoreThanOneBreakLine_WouldBeTaken_TheValue_From_The_Path_And_Trim() {
        final String outKey = "<father><Id>";
        final List<XmlTagPath> tagsToExtract = List.of(
            new XmlTagPath(List.of(
                "father",
                "Id"
            ), outKey)
        );

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, Collections.emptyList(), Collections.emptyList());
        final String content = "\n<Document><Id>somevalue</Id><father>"
            + "\n      \n"
            + "\n<Id>\n  secondvalue\n  </Id>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            outKey, List.of("secondvalue")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenXmlTagHasMoreThanOneChildren_IsTaken_TheValueWithAllThePath() {
        final String outKey = "<father><second><Id>";
        final List<XmlTagPath> tagsToExtract = List.of(
            new XmlTagPath(List.of(
                "father",
                "second",
                "Id"
            ), outKey)
        );
        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, Collections.emptyList(), Collections.emptyList());
        final String content = "<father><first><Id>first</Id></first><second><Id>secondvalue</Id>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            outKey, List.of("secondvalue")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenDocumentHasStopTagBeforeATagToExtract_TheExtractValueWillNotBeExtracted() {
        final String outKey = "<father><second><Id>";
        final List<XmlTagPath> tagsToExtract = List.of(
            new XmlTagPath(List.of(
                "father",
                "second",
                "Id"
            ), outKey)
        );
        final List<String> tagsToStop = List.of("close");

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, tagsToStop, Collections.emptyList());
        final String content = "<close><father></father></close><second><Id>secondvalue</Id>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            outKey, Collections.emptyList()
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenDocumentHasStopTagAfterATagToExtract_TheExtractValueWillBeExtracted() {
        final String outKey = "<father><second><Id>";
        final List<XmlTagPath> tagsToExtract = List.of(
            new XmlTagPath(List.of(
                "father",
                "second",
                "Id"
            ), outKey, 2)
        );
        final List<String> tagsToStop = List.of("close");
        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, tagsToStop, Collections.emptyList());
        final String content =
            "<close>" +
                "<father><second><Id>secondvalue</Id></second></father>" +
                "</close><father><second><Id>other</Id></second></father>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            outKey, List.of("secondvalue")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenTagIsSetWithInfinite_Is_Taken_AllTheMatchesWithThePath() {
        final String outKey = "msgId";
        final int infiniteTimes = -1;
        final List<XmlTagPath> tagsToExtract = List.of(
            new XmlTagPath(List.of("MsgId"),
                outKey, infiniteTimes
            )
        );
        final List<String> tagsToStop = List.of("DrctDbtTxInf");

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, tagsToStop, Collections.emptyList());
        final String content =
            "<Document><DrctDbtTxInf>" +
                "<MsgId>somevalue</MsgId>something<MsgId>secondValue</MsgId>any<MsgId>somevalue</MsgId>" +
                "</DrctDbtTxInf>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(outKey, List.of("somevalue", "secondValue", "somevalue"));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenTagWithMoreThanOneStarterIsSetWithInfinite_Is_Taken_AllTheMatchesWithThePath() {
        final String outKey = "msgId";
        final List<XmlTagPath> tagsToExtract = List.of(
            new XmlTagPath(List.of(
                "MsgId",
                "Id"
            ), outKey, -1)
        );
        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, Collections.emptyList(), Collections.emptyList());
        final String content =
            "<Document><MsgId><Id>somevalue</Id></MsgId><Id>something</Id><MsgId><Id>secondValue</Id>" +
                "</MsgId>any<MsgId><Id>somevalue</Id></MsgId>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(outKey, List.of("somevalue", "secondValue", "somevalue"));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenTagIsSetWithLimitTimes_Is_Taken_TheFirstApparitionsInTheDocument() {
        final List<XmlTagPath> tagsToExtract = List.of(
            new XmlTagPath(List.of("MsgId"), "msgId", 2)
        );

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, Collections.emptyList(), Collections.emptyList());
        final String content =
            "<Document><MsgId>somevalue</MsgId>something<MsgId>secondValue</MsgId>any<MsgId>thirdValue</MsgId>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of("msgId", List.of("somevalue", "secondValue"));
        Assertions.assertEquals(expected, result);
    }

    @Test
    void DocumentHasAnErrorAfterAStopTag_Is_ReturnedProperlyTheValues() {
        final String outKey = "<father><second><Id>";
        final List<XmlTagPath> tagsToExtract = List.of(
            new XmlTagPath(List.of(
                "father",
                "second",
                "Id"
            ), outKey)
        );
        final List<String> tagsToStop = List.of("close");

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, tagsToStop, Collections.emptyList());
        final String content =
            "<father><first><Id>firstvalue</Id></first>" +
                "<second><Id>secondvalue</Id></close>xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.001.001.03\"";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            outKey, List.of("secondvalue")
        );
        Assertions.assertEquals(expected, result);
    }

    //TODO Test with broken values

    @Test
    void whenValueIsAfterSomeOfStopTags_ThisValueIsNotCollected() {
        final String outKey = "<father><second><Id>";
        final List<XmlTagPath> tagsToExtract = List.of(
            new XmlTagPath(List.of(
                "father",
                "second",
                "Id"
            ), outKey, 2)
        );
        final List<String> tagsToStop = List.of("close", "closeDebtor");

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, tagsToStop, Collections.emptyList());
        final String content =
            "<Document><father><first><Id>firstvalue</Id></first>" +
                "<second><Id>secondvalue</Id></second></father><closeDebtor></closeDebtor><father><second><Id>no</Id>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            outKey, List.of("secondvalue")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenDocumentHasSimilarPathsToTheExtract_OnlyIsTaken_TheValueFromTheEntirePath() {
        final String outKey = "<father><second><Id>";
        final List<XmlTagPath> tagsToExtract = List.of(
            new XmlTagPath(List.of(
                "father",
                "second",
                "Id"
            ), outKey)
        );

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, Collections.emptyList(), Collections.emptyList());
        final String content = "<Document>" +
            "<father><first>father</first></father>" +
            "<mother><Id>mother</Id></mother>" +
            "<sister><second><Id>sister</Id></second>" +
            "<father><second><Id>secondvalue</Id></second><third><Id>thirdValue</Id></third>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            outKey, List.of("secondvalue")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenDocumentHasSimilarPathsToTheExtract_OnlyIsTaken_TheValueFromTheEntirePath_Part2() {
        final String outKey = "<father><second><Id>";
        final List<XmlTagPath> tagsToExtract = List.of(
            new XmlTagPath(List.of(
                "father",
                "second",
                "Id"
            ), outKey)
        );
        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, Collections.emptyList(), Collections.emptyList());
        final String content = "<Document>" +
            "<father><first>father</first></father>" +
            "<mother><Id>mother</Id></mother>" +
            "<sister><second><Id>sister</Id></second>" +
            "<father><second>resetall</second></father>" +
            "<father><second><Id>secondvalue</Id></second></father>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of(
            outKey, List.of("secondvalue")
        );
        Assertions.assertEquals(expected, result);
    }

    @Test
    void whenErrorDocumentAppeared_IsReturnedTheValuesCollected() {
        final List<XmlTagPath> tagsToExtract = List.of(new XmlTagPath(List.of("MsgId"), "MsgId", 2));

        final XmlTagExtractor subject = new XmlTagExtractor(tagsToExtract, Collections.emptyList(), Collections.emptyList());
        final String content = "<Document>" +
            "<father><first>father</first></father>" +
            "<mother><Id>mother</Id></mother>" +
            "<sister><second><Id>sister" +
            "<father><MsgId>somevalue</MsgId>" +
            "<father><second><Id>secondvalue</Id></second></father>";
        //Act
        final InputStream input = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        final Map<String, Collection<String>> result = subject.extract("filename", input);
        //Assert
        final var expected = Map.of("MsgId", List.of("somevalue"));
        Assertions.assertEquals(expected, result);
    }
}
