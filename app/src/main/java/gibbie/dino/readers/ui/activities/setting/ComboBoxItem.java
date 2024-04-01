package gibbie.dino.readers.ui.activities.setting;

public class ComboBoxItem {
    private String value;
    private String label;

    public ComboBoxItem(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
