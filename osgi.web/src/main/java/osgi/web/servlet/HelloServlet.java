package osgi.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import dictionary.services.DictionaryService;
import osgi.web.Activator;

/**
 * Hello world!
 *
 */
public class HelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DictionaryService dictionaryService = (DictionaryService) Activator.bundleServices
                .get(DictionaryService.class.getName());
        String word = req.getParameter("word");
        boolean res = dictionaryService.check(word);
        resp.getWriter().write(res ? "'" + word + "' exists" : "'" + word + "' doesn't exist");
    }
}
