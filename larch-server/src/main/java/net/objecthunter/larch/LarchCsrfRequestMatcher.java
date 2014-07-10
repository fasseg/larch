
package net.objecthunter.larch;

import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

public class LarchCsrfRequestMatcher implements RequestMatcher {

    private Pattern allowedMethods = Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");

    @Override
    public boolean matches(HttpServletRequest request) {
        if (request.getContentType() == null) {
            return false;
        }
        // protect HTML forms from Cross Site forgeries using Sessions
        if (request.getContentType().equalsIgnoreCase("multipart/form-data")
                || request.getContentType().equalsIgnoreCase("application/x-www-form-urlencoded")) {
            return true;
        }
        // everything else must authenticate and does not need a CSRF protection
        return false;
    }
}
