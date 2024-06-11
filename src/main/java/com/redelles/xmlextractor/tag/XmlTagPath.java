package com.redelles.xmlextractor.tag;

import java.util.ArrayList;
import java.util.List;

public class XmlTagPath {

    public final List<String> tags;
    public final String outKey;
    /**
     * How many times can appear in the document. If less than 0, there not limit.
     */
    public final int times;

    public XmlTagPath(final String tag) {
        this.tags = List.of(tag);
        this.outKey = tag;
        this.times = 1;
    }

    public XmlTagPath(
        final List<String> startTags,
        final String outKey
    ) {
        this.tags = new ArrayList<>(startTags);
        this.outKey = outKey;
        this.times = 1;
    }

    public XmlTagPath(
        final List<String> startTags,
        final String outKey,
        final int times
    ) {
        this.tags = new ArrayList<>(startTags);
        this.outKey = outKey;
        this.times = times;
    }
}
