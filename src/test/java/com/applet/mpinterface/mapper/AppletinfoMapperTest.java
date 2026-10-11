package com.applet.mpinterface.mapper;

import com.applet.mpinterface.domain.pojo.Appletinfo;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest
@Ignore("Requires an explicitly configured external database")
public class AppletinfoMapperTest {
    @Autowired
    private AppletinfoMapper appletinfoMapper;
    @Test
    public void testSelect()
    {
        QueryWrapper<Appletinfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", "Vwex4QJV");
        List<Appletinfo> appletinfo=  appletinfoMapper.selectList(queryWrapper);
        System.out.println(appletinfo);
    }

}
