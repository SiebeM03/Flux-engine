package me.siebe.flux.lwjgl.gltf.parsers.glb;

import me.siebe.flux.lwjgl.gltf.parsers.AbstractGltfParser;
import me.siebe.flux.util.io.FileFormat;
import me.siebe.flux.util.io.FileIOManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Map;

public class GlbParser extends AbstractGltfParser {

    public GlbParser(Path path) {
        super(path);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void loadFormatSpecificData() throws IOException {
        try (InputStream is = Files.newInputStream(assetFilePath, StandardOpenOption.READ)) {
            BinaryGltfParser.GlbParseResult result = BinaryGltfParser.parse(is);
            json = FileIOManager.get().parse(result.json(), Map.class, FileFormat.JSON);
            buffers = new ArrayList<>();
            if (result.binData() != null) {
                buffers.add(result.binData());
            }
        }
    }

}
