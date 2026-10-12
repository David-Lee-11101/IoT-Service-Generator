package com.applet.mpinterface.service.impl;

import com.applet.mpinterface.domain.pojo.Rundetails;
import com.applet.mpinterface.mapper.RundetailsMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SpecificGeneratorBoundaryTest {
    @TempDir Path root;

    private SpecificGeneratorImpl generator(Object... labels) throws Exception {
        Rundetails record = new Rundetails();
        record.setTriggerNum(1).setFilterNum(0).setDelayNum(0).setQueryNum(0).setActionNum(0);
        java.util.List<Map<String,Object>> ingredients = new java.util.ArrayList<>();
        for (Object label : labels) {
            Map<String,Object> ingredient = new LinkedHashMap<>();
            ingredient.put("label", label); ingredient.put("value", "occupied");
            ingredients.add(ingredient);
        }
        record.setTrigger(new ObjectMapper().writeValueAsString(Collections.singletonList(
                Collections.singletonMap("ingredients", ingredients))));
        RundetailsMapper mapper = mock(RundetailsMapper.class);
        when(mapper.selectOne(any())).thenReturn(record);
        SpecificGeneratorImpl generator = new SpecificGeneratorImpl();
        ReflectionTestUtils.setField(generator, "rundetailsMapper", mapper);
        return generator;
    }

    @Test void unsupportedAtomicMoveFailsClosedAndCleansTemporaryFile() throws Exception {
        Path destination = root.resolve("Example.java");
        assertThrows(java.nio.file.AtomicMoveNotSupportedException.class, () ->
            SpecificGeneratorImpl.writeAtomic(root, destination, "class Example {}", (source, target) -> {
                throw new java.nio.file.AtomicMoveNotSupportedException(source.toString(), target.toString(), "test filesystem");
            }));
        assertFalse(Files.exists(destination));
        try (Stream<Path> files = Files.list(root)) { assertEquals(0, files.count()); }
    }

    @Test void preservesValidIdentifierMethodsAndJavaLiteral() throws Exception {
        generator("roomName").generateAllDetailgetterCode("test", 1, root.toString(), "Example");
        String implementation = new String(Files.readAllBytes(root.resolve("service/impl/SpecificServiceImpl.java")), StandardCharsets.UTF_8);
        assertTrue(implementation.contains("selectRoomName(String id,Integer actionTime)"));
        assertTrue(implementation.contains("\"roomName\".equals"));
        assertTrue(Files.exists(root.resolve("service/impl/ExampleServiceImpl.java")));
    }

    @Test void rejectsMalformedLabelsBeforeWriting() throws Exception {
        for (Object label : Arrays.asList("Room Name", "room\"; }", "room\\name", "class", "", 42, null)) {
            Path output = root.resolve("invalid" + Math.abs(String.valueOf(label).hashCode()));
            SpecificGeneratorImpl generator = generator(label);
            assertThrows(IllegalStateException.class, () -> generator.generateAllDetailgetterCode("test", 1, output.toString(), "Example"));
            assertFalse(Files.exists(output));
        }
    }

    @Test void rejectsDuplicateAndGetterCapitalizationCollisions() throws Exception {
        for (Object[] labels : Arrays.asList(new Object[]{"roomName", "roomName"}, new Object[]{"roomName", "RoomName"})) {
            SpecificGeneratorImpl generator = generator(labels);
            assertThrows(IllegalStateException.class, () -> generator.generateAllDetailgetterCode("test", 1, root.toString(), "Example"));
            try (Stream<Path> files = Files.list(root)) { assertEquals(0, files.count()); }
        }
    }
}
