package io.boxcar.publisher.model;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ListPage<T> {
	List<T> items;
	Integer offset;
	@SerializedName("page_size")
	Integer pageSize;


	public List<T> getItems() {
		return items;
	}
	public Integer getOffset() {
		return offset;
	}
	public Integer getPageSize() {
		return pageSize;
	}
}
