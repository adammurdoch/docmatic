package net.rubygrapefruit.docs.renderer;

import net.rubygrapefruit.docs.model.Block;
import net.rubygrapefruit.docs.model.Component;

import java.util.ArrayList;
import java.util.List;

public class BuildableChunk implements Chunk {
    private final ArrayList<Block> contents = new ArrayList<Block>();
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<? extends Block> getContents() {
        return contents;
    }

    public <T extends Block> T add(T block) {
        contents.add(block);
        return block;
    }

    public BuildableTitleBlock addTitlePage(Component component) {
        return add(new BuildableTitleBlock(component));
    }
}
