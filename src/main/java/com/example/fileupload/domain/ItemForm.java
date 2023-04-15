package com.example.fileupload.domain;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
// form을 가지고 데이터가 왔다갔다 해야함
public class ItemForm {
    private Long itemId;
    private String itemName;
    // 이미지를 다중 업로드 하기 위해서 List에 MultipartFile을 사용
    private List<MultipartFile> imageFiles;
    private MultipartFile attachFile;
}
