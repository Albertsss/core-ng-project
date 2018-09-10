package core.framework.impl.web.request;

import core.framework.util.Encodings;
import core.framework.util.Maps;
import core.framework.web.exception.BadRequestException;

import java.util.Map;

import static core.framework.util.Strings.format;

/**
 * @author neo
 */
public final class PathParams {
    private final Map<String, String> params = Maps.newHashMap();

    public void put(String name, String value) {
        if (value.length() == 0) throw new BadRequestException(format("path param must not be empty, name={}, value={}", name, value));
        try {
            params.put(name, Encodings.decodeURIComponent(value));  // value here is not decoded, see io.undertow.UndertowOptions.DECODE_URL and core.framework.impl.web.HTTPServer
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(e.getMessage(), BadRequestException.DEFAULT_ERROR_CODE, e);
        }
    }

    public String get(String name) {
        String value = params.get(name);
        if (value == null) throw new Error(format("path param not found, name={}", name));
        return value;
    }
}
