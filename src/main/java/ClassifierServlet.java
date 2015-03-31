import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author kalle
 * @since 2015-03-30 16:56
 */
public class ClassifierServlet extends HttpServlet {

  private Classifier classifier;

  @Override
  public void init() throws ServletException {
    try {
      classifier = new Classifier();
      classifier.build();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    response.setHeader("Access-Control-Allow-Origin", "*");

    String namn = request.getParameter("namn");
    String classification;
    try {
      classification = classifier.classify(namn);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    response.setCharacterEncoding("UTF-8");
    response.setContentType("application/json");

    response.getWriter().write("{ \"classification\": \""+classification+"\" }");

  }
}
