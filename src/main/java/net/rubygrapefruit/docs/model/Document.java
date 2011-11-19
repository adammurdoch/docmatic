package net.rubygrapefruit.docs.model;

import java.util.List;

public interface Document {
    List<? extends Paragraph> getParagraphs();
}
