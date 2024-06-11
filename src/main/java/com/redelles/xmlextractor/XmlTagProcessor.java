package com.redelles.xmlextractor;

import com.redelles.xmlextractor.tag.XmlTagPath;

public class XmlTagProcessor {

    public final XmlTagPath tagPath;
    private Integer index = 0;
    private Integer times;

    public XmlTagProcessor(final XmlTagPath tagPath) {
        this.tagPath = tagPath;
        this.times = tagPath.times;
        resetStarters();
    }

    public String getCurrentTag() {
        if (this.index >= this.tagPath.tags.size()) {
            return null;
        }
        return this.tagPath.tags.get(this.index);
    }

    public void next() {
        this.index++;
    }

    public boolean isReadyToWrite() {
        return this.index > this.tagPath.tags.size() - 1;
    }

    public void previous() {
        if (this.index > 0) {
            this.index--;
        }
    }

    public void decreaseTagCount() {
        if (this.times != 0) {
            this.times--;
        }
    }

    public boolean canContinueProcessing() {
        return this.times != 0;
    }

    public String getPreviousTag() {
        if (this.index - 1 < 0) {
            return null;
        }
        return this.tagPath.tags.get(this.index - 1);
    }

    private void resetStarters() {
        this.index = 0;
    }

}
