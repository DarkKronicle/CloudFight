package darkkronicle.github.io.cloudfight.utility;

import darkkronicle.github.io.cloudfight.CloudFight;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

// https://stackoverflow.com/a/19759564

/**
 * Utility class to modify strings and numbers into certain formats.
 */
@UtilityClass
public class FormattingUtils {

    private final TreeMap<Integer, String> map = new TreeMap<>();

    static {
        map.put(1000, "M");
        map.put(900, "CM");
        map.put(500, "D");
        map.put(400, "CD");
        map.put(100, "C");
        map.put(90, "XC");
        map.put(50, "L");
        map.put(40, "XL");
        map.put(10, "X");
        map.put(9, "IX");
        map.put(5, "V");
        map.put(4, "IV");
        map.put(1, "I");
    }

    /**
     * Turns a number into a Roman Numeral.
     *
     * Example: 4 -> IV
     *
     * @param number Example to convert to
     * @return String or Roman Numeral
     */
    public String toRoman(int number) {
        boolean neg = false;
        if (number < 0) {
            neg = true;
            number = -1 * number;
        }
        int l = map.floorKey(number);
        if (number == l) {
            return map.get(number);
        }
        if (neg) {
            return "-" + map.get(l) + toRoman(number - l);
        } else {
            return map.get(l) + toRoman(number - l);
        }
    }

    /**
     * Converts seconds into minute:second format
     *
     * Example: 135 -> 2:15
     *
     * @param seconds Seconds to convert
     * @param frontZeroPad Whether or not a zero should be put in front if the hour is one digit.
     *                     Example: 605 -> 10:05
     *                              505 -> 09:05
     * @return Formatted string
     */
    public String toMinuteSecond(int seconds, boolean frontZeroPad) {
        int minute = seconds / 60;
        int second = seconds % 60;
        String sec = "" + second;
        if (sec.length() == 1) {
            sec = "0" + sec;
        }
        String min = minute + "";
        if (frontZeroPad && min.length() == 1) {
            min = "0" + min;
        }
        return min + ":" + sec;
    }

    /**
     * @author https://pastebin.com/Gy96uT9e https://pastebin.com/Rzz8nhJd https://www.spigotmc.org/threads/free-code-sending-perfectly-centered-chat-message.95872/
     */
    public enum DefaultFontInfo {
        A('A', 5),
        a('a', 5),
        B('B', 5),
        b('b', 5),
        C('C', 5),
        c('c', 5),
        D('D', 5),
        d('d', 5),
        E('E', 5),
        e('e', 5),
        F('F', 5),
        f('f', 4),
        G('G', 5),
        g('g', 5),
        H('H', 5),
        h('h', 5),
        I('I', 3),
        i('i', 1),
        J('J', 5),
        j('j', 5),
        K('K', 5),
        k('k', 4),
        L('L', 5),
        l('l', 1),
        M('M', 5),
        m('m', 5),
        N('N', 5),
        n('n', 5),
        O('O', 5),
        o('o', 5),
        P('P', 5),
        p('p', 5),
        Q('Q', 5),
        q('q', 5),
        R('R', 5),
        r('r', 5),
        S('S', 5),
        s('s', 5),
        T('T', 5),
        t('t', 4),
        U('U', 5),
        u('u', 5),
        V('V', 5),
        v('v', 5),
        W('W', 5),
        w('w', 5),
        X('X', 5),
        x('x', 5),
        Y('Y', 5),
        y('y', 5),
        Z('Z', 5),
        z('z', 5),
        NUM_1('1', 5),
        NUM_2('2', 5),
        NUM_3('3', 5),
        NUM_4('4', 5),
        NUM_5('5', 5),
        NUM_6('6', 5),
        NUM_7('7', 5),
        NUM_8('8', 5),
        NUM_9('9', 5),
        NUM_0('0', 5),
        EXCLAMATION_POINT('!', 1),
        AT_SYMBOL('@', 6),
        NUM_SIGN('#', 5),
        DOLLAR_SIGN('$', 5),
        PERCENT('%', 5),
        UP_ARROW('^', 5),
        AMPERSAND('&', 5),
        ASTERISK('*', 5),
        LEFT_PARENTHESIS('(', 4),
        RIGHT_PARENTHESIS(')', 4),
        MINUS('-', 5),
        UNDERSCORE('_', 5),
        PLUS_SIGN('+', 5),
        EQUALS_SIGN('=', 5),
        LEFT_CURL_BRACE('{', 4),
        RIGHT_CURL_BRACE('}', 4),
        LEFT_BRACKET('[', 3),
        RIGHT_BRACKET(']', 3),
        COLON(':', 1),
        SEMI_COLON(';', 1),
        DOUBLE_QUOTE('"', 3),
        SINGLE_QUOTE('\'', 1),
        LEFT_ARROW('<', 4),
        RIGHT_ARROW('>', 4),
        QUESTION_MARK('?', 5),
        SLASH('/', 5),
        BACK_SLASH('\\', 5),
        LINE('|', 1),
        TILDE('~', 5),
        TICK('`', 2),
        PERIOD('.', 1),
        COMMA(',', 1),
        SPACE(' ', 3),
        DEFAULT('\0', 4);

        private static final Map<Character, DefaultFontInfo> CHAR_MAP = new HashMap<>(values().length, 1.1f);

        private final char character;
        private final int length;


        static {
            for(DefaultFontInfo info : values())
                CHAR_MAP.put(info.character, info);
        }

        DefaultFontInfo(char character, int length) {
            this.character = character;
            this.length = length;
        }

        public char getCharacter() {
            return this.character;
        }

        public int getLength() {
            return this.length;
        }

        public int getBoldLength() {
            if (this == DefaultFontInfo.SPACE) return this.getLength();
            return this.length + 1;
        }

        public static DefaultFontInfo getDefaultFontInfo(char c) {
            return CHAR_MAP.getOrDefault(c, DEFAULT);
        }
    }

    public int getFormattedWidth(String message) {
        if (message == null || message.equals("")) {
            return 0;
        }
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        for (char c : message.toCharArray()){
            if (c == '§') {
                previousCode = true;
            } else if (previousCode){
                previousCode = false;
                isBold = c == 'l' || c == 'L';
            } else {
                DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
                messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
                messagePxSize++;
            }
        }
        return messagePxSize;
    }

    public String centerMessage(String message) {
        return centerMessage(message, " ");
    }

    public String centerMessage(String message, String filler) {
        return centerMessage(message, filler, null, null);
    }

    public String centerMessage(String message, String filler, String start, String end) {
        return centerMessage(message, filler, start, end, 250, false);
    }

    public String centerMessage(String message, String filler, String start, String end, int width, boolean limitFormattiong) {
        if (message != null) {
            message = CloudFight.color(message);
        }
        if (filler != null) {
            filler = CloudFight.color(filler);
        }
        if (start != null) {
            start = CloudFight.color(start);
        }
        if (end != null) {
            end = CloudFight.color(end);
        }

        int messagePxSize = getFormattedWidth(message);
        int startWidth = getFormattedWidth(start);
        int endWidth = getFormattedWidth(end);
        int fillerLength = getFormattedWidth(filler);

        // Prevent infinite loops
        if (fillerLength == 0) {
            filler = " ";
            fillerLength = getFormattedWidth(filler);
        }

        // We subtract startWidth and endWidth because we'll add those later (they default to 0)
        int TOTAL_PX = width - startWidth - endWidth;
        int CENTER_PX = (int) Math.floor((float) TOTAL_PX / 2);

        int halvedMessageSize = messagePxSize / 2;
        int toCompensate = CENTER_PX - halvedMessageSize;

        int compensated = 0;
        StringBuilder sb = new StringBuilder("§r");
        // Start
        if (start != null) {
            sb.append(start);
        }
        if (limitFormattiong) {
            sb.append("§r");
        }
        // First filler
        while(compensated < toCompensate){
            sb.append(filler);
            compensated += fillerLength;
        }
        if (limitFormattiong) {
            sb.append("§r");
        }
        // Message
        if (message != null) {
            sb.append(message);
        }
        if (limitFormattiong) {
            sb.append("§r");
        }
        if (end != null || !filler.equals(" ")) {
            compensated += messagePxSize;
            // Second filler
            while (compensated < TOTAL_PX) {
                sb.append(filler);
                compensated += fillerLength;
            }
            if (limitFormattiong) {
                sb.append("§r");
            }
            // End
            if (end != null) {
                sb.append(end);
            }
        }
        sb.append("§r");
        return sb.toString();
    }

    public String[] wrap(String message, int width) {
        ArrayList<String> lines = new ArrayList<>(Arrays.asList(message.split("\n")));
        ArrayList<String> newLines = new ArrayList<>();
        for (String l : lines) {
            String[] words = l.split(" ");
            ArrayList<String> line = new ArrayList<>();
            int currentWidth = 0;
            for (String w : words) {
                int ww = getFormattedWidth(w);
                if (ww > width) {
                    StringBuilder n = new StringBuilder();
                    while (ww > width) {
                        n.insert(0, w.toCharArray()[w.length() - 1]);
                        w = w.substring(0, w.length() - 1);
                        ww = getFormattedWidth(w);
                    }
                    line.add(w);
                    lines.add(String.join(" ", w));
                    line = new ArrayList<>(Collections.singleton(n.toString()));
                    continue;
                }
                int newWidth = currentWidth + ww;
                if (newWidth <= width) {
                    line.add(w);
                } else {
                    newLines.add(String.join(" ", line));
                    line = new ArrayList<>(Collections.singleton(w));
                }
                currentWidth = newWidth;
            }
            newLines.add(String.join(" ", line));
        }
        return newLines.toArray(new String[0]);
    }


}
