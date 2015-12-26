package org.skellig.feature;

import java.util.Set;

public class TagDetails implements TestPreRequisites<TagDetails> {

    private Set<String> tags;

    public TagDetails(Set<String> tags) {
        this.tags = tags;
    }

    public Set<String> getTags() {
        return tags;
    }

    @Override
    public TagDetails getDetails() {
        return this;
    }
}
