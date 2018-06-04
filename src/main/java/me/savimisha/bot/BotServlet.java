package me.savimisha.bot;

import com.pengrad.telegrambot.BotUtils;
import com.pengrad.telegrambot.model.Update;
import me.savimisha.Config;
import me.savimisha.utils.DataBase;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet(name = "me.savimisha.bot.BotServlet", urlPatterns = { Config.BOT_SERVLET_PATH}, loadOnStartup = 1)
@MultipartConfig
public class BotServlet extends HttpServlet {

    private static final String TAG = BotServlet.class.getSimpleName();

    private static Bot bot = new Bot();

    @Override
    public void init() throws ServletException {
        if (!bot.isWebhookSet()) bot.setWebhook();
        super.init();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Update update = BotUtils.parseUpdate(req.getReader());

            try {
                DataBase.update(update);
            } catch (SQLException e) {
                DataBase.error(TAG, null, e);
            }

            bot.onUpdateReceived(update);
        }catch (Exception e){
            DataBase.error(TAG, "Unknown exception.:(", e);
        }
    }
}
