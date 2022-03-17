package parser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ParserTest {

    private final Pattern pattern = Pattern.compile("[\\$|\\#]\\{(.*?)\\}");

    @Test
    @DisplayName("패턴 매칭 테스트")
    public void Pattern_매칭_테스트() {
        Map<String, Object> map = new HashMap<>();
        map.put("ID", "LEEBONGHO");
        map.put("AGE", 30);
        map.put("HEIGHT", 170.1);

        String script = "SELECT A, B, C FROM table WHERE ID=#{ID} AND AGE=#{AGE} AND HEIGHT=#{HEIGHT}";
        String expected = "SELECT A, B, C FROM table WHERE ID='LEEBONGHO' AND AGE=30 AND HEIGHT=170.1";

        Matcher matcher = pattern.matcher(script);
        while (matcher.find()) {
            String fParam = matcher.group();
            String fId = fParam.replaceAll("[\\$|\\#]\\{","").replaceAll("\\}","");

            script = script.replace(fParam, getValue(map, fId));
            System.out.println(script);
        }

        assertThat(script, is(expected));
    }

    private String getValue(Map<String, Object> map, String key) {
        if (map.containsKey(key)) {
            String result = String.valueOf(map.get(key));

            if (map.get(key) instanceof Number) {
                return result;
            }

            if (map.get(key) instanceof String) {
                return "'" + result + "'";
            }
        }

        return "";
    }

}
