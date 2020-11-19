package com.bot.base.chain.menu;

import com.bot.base.chain.Menu;
import com.bot.common.constant.BaseConsts;
import org.springframework.stereotype.Component;

/**
 * @author murongyehua
 * @version 1.0 2020/9/25
 */
@Component("answerMenuPrinter")
public class AnswerMenuPrinter extends Menu {

    AnswerMenuPrinter() {
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = BaseConsts.Menu.ANSWER_BOOK;
        this.describe = BaseConsts.AnswerBook.DESCRIBE;
    }
}
