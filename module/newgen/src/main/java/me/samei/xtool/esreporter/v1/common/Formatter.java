package me.samei.xtool.esreporter.v1.common;

import java.awt.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Formatter {

    public String formatKey(String key);

    public Value formatString(String rawKey, String rawValue);

    public Value formatNum(String rawKey, java.lang.Number rawValue);



    public static class Default implements Formatter {

        protected String keysInvalidRegex = "[\\\\|/\"'\\n\\r]";
        protected Pattern keysInvalidPattern = Pattern.compile(keysInvalidRegex);
        public boolean isValidKey(String key) { return keysInvalidPattern.matcher(key).find(); }
        public void checkKeyValidity(String key) {
            if (!isValidKey(key)) throw new RuntimeException("Invlaid Key: '"+ key + "'");
        }

        protected String valuesInvalidRegex = "[\"\\n\\r]";
        protected Pattern valuesInvalidPattern = Pattern.compile(valuesInvalidRegex);
        public boolean isValidStringValue(String value) { return valuesInvalidPattern.matcher(value).find(); }
        public void checkStringValueValidity(String value) {
            if (!isValidStringValue(value)) throw new RuntimeException("Invlaid Value: '"+ value + "'");
        }

        @Override
        public String formatKey(String key) {
            checkKeyValidity(key);
            return key;
        }

        @Override
        public Value formatString(String rawKey, String rawValue) {

            String key = formatKey(rawKey);
            checkStringValueValidity(rawValue);

            ArrayList<String> tags = new ArrayList<>();
            tags.add("string");
            tags.add("qouted");

            return new Value(
                    key,
                    rawValue,
                    Value.Type.Quoted,
                    tags
            );
        }

        @Override
        public Value formatNum(String rawKey, Number rawValue) {

            String key = formatKey(rawKey);

            ArrayList<String> tags = new ArrayList<>();
            tags.add("number");

            return new Value(
                    key,
                    rawValue.toString(),
                    Value.Type.Simple,
                    tags
            );
        }
    }

}



