package com.rips7.day;

import processing.core.PApplet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Day5 implements Day<Long> {

    private List<VisualRange> inputVisualRanges;
    private List<VisualRange> mergedVisualRanges;
    private List<VisualId> inputVisualIds;

    private List<Long> tickPoints;
    private Map<Long, Integer> pointToIndexMap;

    private AnimationPhase currentPhase = AnimationPhase.PARSE_INPUT;
    private int animationStep = 0;
    private long currentTotalCount = 0;
    private int currentInputRangeIndex = 0;
    private int currentInputIdIndex = 0;
    private int currentMergedRangeIndex = 0;
    private boolean isPart2Setup = false;

    private enum AnimationPhase {
        PARSE_INPUT,
        MERGING_RANGES,
        PART1_CHECKING_IDS,
        PART2_SUMMING_RANGES,
        DONE
    }

    @Override
    public Long part1(String input) {
        final String[] sections = input.split("\\R\\R", 2);

        final List<Range> flattenedRanges = sections[0].lines()
                .map(Range::parse)
                // Sort and merge overlapping ranges to avoid unnecessary comparisons
                .sorted(Comparator.comparingLong(Range::startId))
                .collect(ArrayList::new, Range.rangeMerger(), ArrayList::addAll);

        return sections[1].lines()
                .map(Long::parseLong)
                .filter(id -> flattenedRanges.stream().anyMatch(range -> range.contains(id)))
                .count();
    }

    @Override
    public Long part2(String input) {
        final String[] sections = input.split("\\R\\R", 2);

        final List<Range> flattenedRanges = sections[0].lines()
                .map(Range::parse)
                // Sort and merge overlapping ranges to avoid unnecessary comparisons
                .sorted(Comparator.comparingLong(Range::startId))
                .collect(ArrayList::new, Range.rangeMerger(), ArrayList::addAll);

        return flattenedRanges.stream()
                .mapToLong(Range::numIds)
                .sum();
    }

    @Override
    public boolean visualize() {
        return Day.super.visualize();
//        return true;
    }

    @Override
    public void setupVisuals(PApplet canvas, String input) {
        String[] sections = input.split("\\R\\R", 2);
        List<Range> rawRanges = sections[0].lines().map(Range::parse).sorted(Comparator.comparingLong(Range::startId)).toList();
        List<Long> rawIds = (sections.length > 1) ? sections[1].lines().map(Long::parseLong).toList() : List.of();

        TreeSet<Long> uniquePoints = new TreeSet<>();
        rawRanges.forEach(r -> {
            uniquePoints.add(r.startId());
            uniquePoints.add(r.endId());
        });
        uniquePoints.addAll(rawIds);

        this.tickPoints = new ArrayList<>(uniquePoints);
        this.pointToIndexMap = new HashMap<>();
        for (int i = 0; i < tickPoints.size(); i++) {
            pointToIndexMap.put(tickPoints.get(i), i);
        }

        inputVisualRanges = rawRanges.stream().map(r -> new VisualRange(r, VisualRangeState.INPUT_RAW)).collect(Collectors.toList());
        inputVisualIds = rawIds.stream().map(id -> new VisualId(id, VisualIdState.UNCHECKED)).collect(Collectors.toList());
        mergedVisualRanges = new ArrayList<>();

        currentPhase = AnimationPhase.PARSE_INPUT;
        animationStep = 0;
        currentTotalCount = 0;
        currentInputRangeIndex = 0;
        currentInputIdIndex = 0;
        currentMergedRangeIndex = 0;

        canvas.textAlign(PApplet.CENTER, PApplet.CENTER);
        canvas.frameRate(30);
    }

    @Override
    public boolean drawPart1(PApplet canvas) {
        return runVisualization(canvas, "Part 1", true);
    }

    @Override
    public boolean drawPart2(PApplet canvas) {
        if (!isPart2Setup) {
            setupVisuals(canvas, Day.super.loadInput());
            isPart2Setup = true;
        }
        return runVisualization(canvas, "Part 2", false);
    }

    private boolean runVisualization(PApplet canvas, String title, boolean isPart1) {
        drawBackground(canvas, title);
        drawRanges(canvas);
        if (isPart1) {
            drawIds(canvas);
        }
        drawCounters(canvas, isPart1);

        switch (currentPhase) {
            case PARSE_INPUT:
                if (animationStep++ > 10) {
                    currentPhase = AnimationPhase.MERGING_RANGES;
                    animationStep = 0;
                }
                break;

            case MERGING_RANGES:
                if (currentInputRangeIndex >= inputVisualRanges.size()) {
                    currentPhase = isPart1 ? AnimationPhase.PART1_CHECKING_IDS : AnimationPhase.PART2_SUMMING_RANGES;
                    break;
                }

                VisualRange currentInputRange = inputVisualRanges.get(currentInputRangeIndex);
                currentInputRange.setState(VisualRangeState.MERGING_CURRENT);

                if (mergedVisualRanges.isEmpty() || mergedVisualRanges.getLast().getRange().isOutside(currentInputRange.getRange())) {
                    mergedVisualRanges.add(new VisualRange(currentInputRange.getRange(), VisualRangeState.MERGED_ACTIVE));
                } else {
                    VisualRange lastMerged = mergedVisualRanges.removeLast();
                    Range merged = lastMerged.getRange().merge(currentInputRange.getRange());
                    mergedVisualRanges.add(new VisualRange(merged, VisualRangeState.MERGED_ACTIVE));
                }
                currentInputRangeIndex++;
                break;

            case PART1_CHECKING_IDS:
                if (currentInputIdIndex >= inputVisualIds.size()) {
                    currentPhase = AnimationPhase.DONE;
                    break;
                }
                VisualId currentId = inputVisualIds.get(currentInputIdIndex);
                currentId.setState(VisualIdState.TESTING);

                if (mergedVisualRanges.stream().anyMatch(vr -> vr.getRange().contains(currentId.getId()))) {
                    currentId.setState(VisualIdState.CONTAINED);
                    currentTotalCount++;
                } else {
                    currentId.setState(VisualIdState.NOT_CONTAINED);
                }
                currentInputIdIndex++;
                break;

            case PART2_SUMMING_RANGES:
                if (currentMergedRangeIndex >= mergedVisualRanges.size()) {
                    currentPhase = AnimationPhase.DONE;
                    break;
                }
                VisualRange rangeToSum = mergedVisualRanges.get(currentMergedRangeIndex);
                rangeToSum.setState(VisualRangeState.MERGED_SUMMING);
                currentTotalCount += rangeToSum.getRange().numIds();
                currentMergedRangeIndex++;
                break;

            case DONE:
                return false;
        }
        return true;
    }

    private void drawBackground(PApplet canvas, String title) {
        canvas.background(0);
        canvas.fill(255);
        canvas.textSize(24);
        canvas.text(title, canvas.width / 2f, 30);
        canvas.textSize(16);
        canvas.text("Phase: " + currentPhase.name().replace("_", " "), canvas.width / 2f, 75);
    }

    private void drawRanges(PApplet canvas) {
        float rangeHeight = 30;
        float inputY = 150;
        float mergedY = 250;

        canvas.fill(200);
        canvas.textSize(16);
        canvas.textAlign(PApplet.LEFT, PApplet.CENTER);
        canvas.text("Input Ranges (Overlapping)", 50, inputY - 25);
        canvas.text("Merged Ranges", 50, mergedY - 25);
        canvas.textAlign(PApplet.CENTER, PApplet.CENTER);

        for (VisualRange vr : inputVisualRanges) {
            drawRange(canvas, vr, inputY, rangeHeight, 100);
        }
        if (currentPhase == AnimationPhase.MERGING_RANGES && currentInputRangeIndex < inputVisualRanges.size()) {
            drawRange(canvas, inputVisualRanges.get(currentInputRangeIndex), inputY, rangeHeight, 255);
        }

        for (VisualRange vr : mergedVisualRanges) {
            drawRange(canvas, vr, mergedY, rangeHeight, 200);
        }
    }

    private void drawRange(PApplet canvas, VisualRange vr, float y, float h, int alpha) {
        float x1 = mapIdToX(canvas, vr.getRange().startId());
        float x2 = mapIdToX(canvas, vr.getRange().endId());
        VisualRangeState state = vr.getState();
        canvas.fill(state.r, state.g, state.b, alpha);
        canvas.noStroke();
        canvas.rect(x1, y, Math.max(1, x2 - x1), h);
    }

    private void drawIds(PApplet canvas) {
        if (inputVisualIds.isEmpty()) return;
        float idY = 350;
        canvas.fill(200);
        canvas.textAlign(PApplet.LEFT, PApplet.CENTER);
        canvas.text("Ids", 50, idY - 25);
        for (VisualId vid : inputVisualIds) {
            float x = mapIdToX(canvas, vid.getId());
            canvas.stroke(vid.getState().color(canvas));
            canvas.strokeWeight(2);
            canvas.line(x, idY - 5, x, idY + 5);
        }
    }

    private void drawCounters(PApplet canvas, boolean isPart1) {
        canvas.fill(0, 255, 0);
        canvas.textSize(28);
        String label = isPart1 ? "Found IDs: " : "Total Covered IDs: ";
        canvas.text(label + String.format("%,d", currentTotalCount), canvas.width / 2f, canvas.height - 40);
    }

    private float mapIdToX(PApplet canvas, long id) {
        Integer index = pointToIndexMap.get(id);
        if (index == null) return -1;
        return mapIndexToX(canvas, index);
    }

    private float mapIndexToX(PApplet canvas, int index) {
        return PApplet.map(index, 0, tickPoints.size() - 1, 50, canvas.width - 50);
    }

    private record Range(long startId, long endId) {
        private static Range parse(final String input) {
            final String[] parts = input.split("-");
            return new Range(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
        }

        private static BiConsumer<ArrayList<Range>, Range> rangeMerger() {
            return (list, range) -> {
                if (list.isEmpty() || list.getLast().isOutside(range)) {
                    list.add(range);
                } else {
                    list.set(list.size() - 1, list.getLast().merge(range));
                }
            };
        }

        private boolean isOutside(final Range other) {
            return this.startId > other.endId || other.startId > this.endId;
        }

        private boolean contains(final long id) {
            return this.startId <= id && id <= this.endId;
        }

        private Range merge(Range other) {
            return new Range(Math.min(this.startId, other.startId), Math.max(this.endId, other.endId));
        }

        private long numIds() {
            return this.endId - this.startId + 1;
        }
    }

    private enum VisualRangeState {
        INPUT_RAW(100, 100, 255),
        MERGING_CURRENT(255, 165, 0),
        MERGED_ACTIVE(0, 200, 0),
        MERGED_SUMMING(0, 255, 255);

        private final int r, g, b;
        VisualRangeState(int r, int g, int b) { this.r = r; this.g = g; this.b = b; }
    }

    private static class VisualRange {
        private final Range range;
        private VisualRangeState state;
        public VisualRange(Range range, VisualRangeState state) { this.range = range; this.state = state; }
        public Range getRange() { return range; }
        public VisualRangeState getState() { return state; }
        public void setState(VisualRangeState state) { this.state = state; }
    }

    private enum VisualIdState {
        UNCHECKED(150),
        TESTING(255, 255, 0),
        CONTAINED(0, 255, 0),
        NOT_CONTAINED(255, 0, 0);

        private final int r, g, b;
        VisualIdState(int gray) { this(gray, gray, gray); }
        VisualIdState(int r, int g, int b) { this.r = r; this.g = g; this.b = b; }
        public int color(PApplet canvas) { return canvas.color(r, g, b); }
    }

    private static class VisualId {
        private final long id;
        private VisualIdState state;
        public VisualId(long id, VisualIdState state) { this.id = id; this.state = state; }
        public long getId() { return id; }
        public VisualIdState getState() { return state; }
        public void setState(VisualIdState state) { this.state = state; }
    }
}
