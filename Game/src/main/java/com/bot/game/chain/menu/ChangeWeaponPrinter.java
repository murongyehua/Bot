package com.bot.game.chain.menu;

import cn.hutool.core.collection.CollectionUtil;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.PlayerWeapon;
import com.bot.game.dao.mapper.PlayerWeaponMapper;

import java.util.List;

/**
 * @author liul
 * @version 1.0 2020/11/4
 */
public class ChangeWeaponPrinter extends Menu {

    ChangeWeaponPrinter() {
        this.initMenu();
    }
    @Override
    public void initMenu() {
        this.menuName = GameConsts.LittlePrinter.CHANGE_WEAPON;
    }

    @Override
    public void getDescribe(String token) {
        PlayerWeaponMapper playerWeaponMapper = (PlayerWeaponMapper) mapperMap.get(GameConsts.MapperName.PLAYER_WEAPON);
        PlayerWeapon param = new PlayerWeapon();
        param.setPlayerId(token);
        List<PlayerWeapon> list = playerWeaponMapper.selectBySelective(param);
        if (CollectionUtil.isEmpty(list)) {
            this.describe = GameConsts.Weapon.NO_WEAPON;
            return;
        }
        for (int index=0; index < list.size(); index++) {
            this.menuChildrenMap.put(String.valueOf(index + 1), new WeaponDetailPrinter(list.get(index)));
        }
        this.describe = GameConsts.Weapon.WAIT_WEAPON;
    }


}
