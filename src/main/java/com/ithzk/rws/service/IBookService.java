package com.ithzk.rws.service;

import com.ithzk.rws.entity.Book;

/**
 * @author hzk
 * @date 2019/3/26
 */
public interface IBookService {
    int insertSelective(Book record);

    Book selectByPrimaryKey(Integer id);
}
