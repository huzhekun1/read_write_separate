package com.ithzk.rws;

import com.ithzk.rws.dao.BookMapper;
import com.ithzk.rws.entity.Book;
import com.ithzk.rws.service.IBookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author hzk
 * @date 2019/3/26
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-mybatis.xml")
public class AppTest {

    @Autowired
    private BookMapper bookMapper;
    @Autowired
    private IBookService bookService;

    @Test
    public void insertTest(){
        Book book = new Book("道德经", 11.11);
        bookMapper.insertSelective(book);
    }

    @Test
    public void selectTest(){
        System.out.println(bookMapper.selectByPrimaryKey(1));
    }

    @Test
    public void test(){
        Book book = new Book("道德经", 11.11);
        bookMapper.insertSelective(book);
        System.out.println(bookMapper.selectByPrimaryKey(1));
    }

    @Test
    public void testAop(){
        Book book = new Book("道德经", 11.11);
        bookService.insertSelective(book);
        System.out.println(bookService.selectByPrimaryKey(1));
    }
}
