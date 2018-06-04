package me.savimisha;

import me.savimisha.utils.DataBase;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(name = "wakeup", urlPatterns = {Config.WAKEUP_SERVLET_PATH}, loadOnStartup = 1)
@MultipartConfig
public class WakeupServlet extends HttpServlet {
    @Override
    public void init() throws ServletException {
        super.init();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Content-Type", "text/plain");
        PrintWriter writer = resp.getWriter();
        writer.write("OK\n");
        writer.close();
        DataBase.info(WakeupServlet.class.getSimpleName(), "Wakeup");
    }
}
