package com.bot.game.chain.menu;

import com.bot.commom.constant.BaseConsts;
import com.bot.commom.constant.GameConsts;
import com.bot.game.chain.Menu;
import com.bot.game.dao.entity.BaseWeapon;
import com.bot.game.dao.entity.PlayerWeapon;
import com.bot.game.dao.mapper.BaseWeaponMapper;
import com.bot.game.enums.ENWeaponEffect;
import com.bot.game.service.impl.ChangeWeaponServiceImpl;

/**
 * @author liul
 * @version 1.0 2020/11/4
 */
public class WeaponDetailPrinter extends Menu {

    private PlayerWeapon playerWeapon;

    private BaseWeapon baseWeapon;

    WeaponDetailPrinter(PlayerWeapon playerWeapon) {
        this.playerWeapon = playerWeapon;
        BaseWeaponMapper weaponMapper = (BaseWeaponMapper) mapperMap.get(GameConsts.MapperName.BASE_WEAPON);
        this.baseWeapon = weaponMapper.selectByPrimaryKey(playerWeapon.getWeaponId());
        this.initMenu();
    }

    @Override
    public void initMenu() {
        this.menuName = String.format("%s[灵气%s级]", baseWeapon.getName(), playerWeapon.getLevel());
    }

    @Override
    public void getDescribe(String token) {
        ENWeaponEffect enWeaponEffect = ENWeaponEffect.getByValue(baseWeapon.getEffect());
        this.describe = String.format(GameConsts.Weapon.WEAPON_DETAIL, String.format(enWeaponEffect.getEffectContent(),
                enWeaponEffect.getLevelNumber()[playerWeapon.getLevel() - 1]), playerWeapon.getLevel(), baseWeapon.getDescribe());
        this.playServiceMap.put(BaseConsts.Menu.ONE, new ChangeWeaponServiceImpl(playerWeapon));
    }


}
