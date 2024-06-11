package com.redelles.xmlextractor;

import com.redelles.xmlextractor.XmlTagProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.redelles.xmlextractor.tag.XmlTagPath;

import java.util.List;

class TagProcessorTest {

    @Test
    void wheProcessorIsCreated_CurrentValueIsFirst_And_PreviousIsNull() {

        final var one = "<one>";
        final var second = "<second>";
        final var third = "<third>";
        final var processor = new XmlTagProcessor(new XmlTagPath(List.of(one, second, third), ""));

        final var current = processor.getCurrentTag();
        final var previous = processor.getPreviousTag();

        Assertions.assertEquals(one, current);
        Assertions.assertNull(previous);
        Assertions.assertFalse(processor.isReadyToWrite());
    }

    @Test
    void wheProcessorIsNext_CurrentValueIsSecond_And_PreviousIsFirst() {

        final var one = "<one>";
        final var second = "<second>";
        final var third = "<third>";
        final var processor = new XmlTagProcessor(new XmlTagPath(List.of(one, second, third), ""));

        processor.next();
        final var current = processor.getCurrentTag();
        final var previous = processor.getPreviousTag();

        Assertions.assertEquals(second, current);
        Assertions.assertEquals(one, previous);
        Assertions.assertFalse(processor.isReadyToWrite());
    }

    @Test
    void wheProcessorIsPrevious_CurrentValueIsFirst_And_PreviousIsNull() {

        final var one = "<one>";
        final var second = "<second>";
        final var third = "<third>";
        final var processor = new XmlTagProcessor(new XmlTagPath(List.of(one, second, third), ""));

        processor.previous();
        final var current = processor.getCurrentTag();
        final var previous = processor.getPreviousTag();

        Assertions.assertEquals(one, current);
        Assertions.assertNull(previous);
        Assertions.assertFalse(processor.isReadyToWrite());
    }

    @Test
    void wheProcessorIsNextTwice_CurrentValueIsThird_And_PreviousIsSecond() {

        final var one = "<one>";
        final var second = "<second>";
        final var third = "<third>";
        final var processor = new XmlTagProcessor(new XmlTagPath(List.of(one, second, third), ""));

        processor.next();
        processor.next();
        final var current = processor.getCurrentTag();
        final var previous = processor.getPreviousTag();

        Assertions.assertEquals(third, current);
        Assertions.assertEquals(second, previous);
        Assertions.assertFalse(processor.isReadyToWrite());
    }

    @Test
    void wheProcessorIsNextTwiceAndPrevious_CurrentValueIsSecond_And_PreviousIsFirst() {

        final var one = "<one>";
        final var second = "<second>";
        final var third = "<third>";
        final var processor = new XmlTagProcessor(new XmlTagPath(List.of(one, second, third), ""));

        processor.next();
        processor.next();
        processor.previous();
        final var current = processor.getCurrentTag();
        final var previous = processor.getPreviousTag();

        Assertions.assertEquals(second, current);
        Assertions.assertEquals(one, previous);
        Assertions.assertFalse(processor.isReadyToWrite());
    }

    @Test
    void wheProcessorIsThreeTimes_CurrentValueIsNull_And_PreviousIsThird_AndIsReadyToWrite() {

        final var one = "<one>";
        final var second = "<second>";
        final var third = "<third>";
        final var processor = new XmlTagProcessor(new XmlTagPath(List.of(one, second, third), ""));

        processor.next();
        processor.next();
        processor.next();
        final var current = processor.getCurrentTag();
        final var previous = processor.getPreviousTag();

        Assertions.assertNull(current);
        Assertions.assertEquals(third, previous);
        Assertions.assertTrue(processor.isReadyToWrite());
    }

    @Test
    void wheProcessorIsThreeTimesAndPrevious_CurrentValueThird_And_PreviousIsSecond_AndNotReadyToWrite() {

        final var one = "<one>";
        final var second = "<second>";
        final var third = "<third>";
        final var processor = new XmlTagProcessor(new XmlTagPath(List.of(one, second, third), ""));

        processor.next();
        processor.next();
        processor.next();
        processor.previous();

        final var current = processor.getCurrentTag();
        final var previous = processor.getPreviousTag();

        Assertions.assertEquals(third, current);
        Assertions.assertEquals(second, previous);
        Assertions.assertFalse(processor.isReadyToWrite());
    }

}