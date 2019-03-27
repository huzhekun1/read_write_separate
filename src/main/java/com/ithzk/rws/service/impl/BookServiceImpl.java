package com.ithzk.rws.service.impl;

import com.ithzk.rws.dao.BookMapper;
import com.ithzk.rws.entity.Book;
import com.ithzk.rws.service.IBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author hzk
 * @date 2019/3/26
 */
@Service
public class BookServiceImpl implements IBookService {

    @Autowired
    private BookMapper bookMapper;

    @Override
    public int insertSelective(Book record){
        return bookMapper.insertSelective(record);
    }

    @Override
    public Book selectByPrimaryKey(Integer id){
        return bookMapper.selectByPrimaryKey(id);
    }
}
