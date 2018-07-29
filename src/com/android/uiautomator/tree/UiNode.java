package com.android.uiautomator.tree;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UiNode
        extends BasicTreeNode {
    private static final Pattern BOUNDS_PATTERN = Pattern.compile("\\[-?(\\d+),-?(\\d+)\\]\\[-?(\\d+),-?(\\d+)\\]");
    private final Map<String, String> mAttributes = new LinkedHashMap();
    private String mDisplayName = "ShouldNotSeeMe";
    private Object[] mCachedAttributesArray;

    public void addAtrribute(String key, String value) {
        this.mAttributes.put(key, value);
        updateDisplayName();
        if ("bounds".equals(key)) {
            updateBounds(value);
        }
    }

    public Map<String, String> getAttributes() {
        return Collections.unmodifiableMap(this.mAttributes);
    }

    private void updateDisplayName() {
        String className = (String) this.mAttributes.get("class");
        if (className == null) {
            return;
        }
        String text = (String) this.mAttributes.get("text");
        if (text == null) {
            return;
        }
        String contentDescription = (String) this.mAttributes.get("content-desc");
        if (contentDescription == null) {
            return;
        }
        String index = (String) this.mAttributes.get("index");
        if (index == null) {
            return;
        }
        String bounds = (String) this.mAttributes.get("bounds");
        if (bounds == null) {
            return;
        }
        className = className.replace("android.widget.", "");
        className = className.replace("android.view.", "");
        StringBuilder builder = new StringBuilder();
        builder.append('(');
        builder.append(index);
        builder.append(") ");
        builder.append(className);
        if (!text.isEmpty()) {
            builder.append(':');
            builder.append(text);
        }
        if (!contentDescription.isEmpty()) {
            builder.append(" {");
            builder.append(contentDescription);
            builder.append('}');
        }
        builder.append(' ');
        builder.append(bounds);
        this.mDisplayName = builder.toString();
    }

    private void updateBounds(String bounds) {
        Matcher m = BOUNDS_PATTERN.matcher(bounds);
        if (m.matches()) {
            this.x = Integer.parseInt(m.group(1));
            this.y = Integer.parseInt(m.group(2));
            this.width = (Integer.parseInt(m.group(3)) - this.x);
            this.height = (Integer.parseInt(m.group(4)) - this.y);
            this.mHasBounds = true;
        } else {
            throw new RuntimeException("Invalid bounds: " + bounds);
        }
    }

    public String toString() {
        return this.mDisplayName;
    }

    public String getAttribute(String key) {
        return (String) this.mAttributes.get(key);
    }

    public Object[] getAttributesArray() {
        int i;
        if (this.mCachedAttributesArray == null) {
            this.mCachedAttributesArray = new Object[this.mAttributes.size()];
            i = 0;
            for (String attr : this.mAttributes.keySet()) {
                this.mCachedAttributesArray[(i++)] = new AttributePair(attr, (String) this.mAttributes.get(attr));
            }
        }
        return this.mCachedAttributesArray;
    }
}
