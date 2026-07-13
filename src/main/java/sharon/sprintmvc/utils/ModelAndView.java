package sharon.sprintmvc.utils;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {

    private String view;
    private Map<String, Object> values;

    public ModelAndView() {
        this.values = new HashMap<>();
    }

    public ModelAndView(String view, Map<String, Object> values) {
        this.view = view;
        this.values = values;
    }

    public String getView() { return view; }
    public void setView(String view) { this.view = view; }

    public Map<String, Object> getValues() { return values; }
    public void setValues(Map<String, Object> values) { this.values = values; }

    public void addValue(String key, Object value) {
        this.values.put(key, value);
    }
}