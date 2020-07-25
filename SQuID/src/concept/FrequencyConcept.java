package concept;

public class FrequencyConcept {
    String key;
    String value;
    Integer minFrequency;
    Integer maxFrequency;
    Integer absoluteMinFrequency;
    Integer absoluteMaxFrequency;
    boolean dropped = true;
    private double confidence;
    private boolean droppedDueToLowFScore = false;

    public FrequencyConcept(String key, String value, Integer minFrequency, Integer maxFrequency,
            Integer absoluteMinFreq, Integer absoluteMaxFreq) {
        this.key = key;
        this.value = value;
        this.minFrequency = minFrequency;
        this.maxFrequency = maxFrequency;
        this.absoluteMinFrequency = absoluteMinFreq;
        this.absoluteMaxFrequency = absoluteMaxFreq;

    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public int getFrequencyMin() {
        return minFrequency;
    }

    public int getFrequencyMax() {
        return maxFrequency;
    }

    public int getFrequencyAbsMin() {
        return absoluteMinFrequency;
    }

    public int getFrequencyAbsMax() {
        return absoluteMaxFrequency;
    }

    public void setDropped(boolean b) {
        dropped = b;
    }

    public boolean isDropped() {
        return dropped;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setDroppedDueToLowFScore() {
        droppedDueToLowFScore = true;
    }

    public boolean isDroppedDueToLowFScore() {
        return droppedDueToLowFScore;
    }
}