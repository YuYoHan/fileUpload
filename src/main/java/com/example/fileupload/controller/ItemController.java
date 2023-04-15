package com.example.fileupload.controller;

import com.example.fileupload.domain.Item;
import com.example.fileupload.domain.ItemForm;
import com.example.fileupload.domain.UploadFile;
import com.example.fileupload.file.FileStore;
import com.example.fileupload.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final FileStore fileStore;

    @GetMapping("/items/new")
    public String newItem(@ModelAttribute ItemForm form) {
        return "item-form";
    }

    @PostMapping("/items/new")
    public String saveItem(@ModelAttribute ItemForm form, RedirectAttributes redirectAttributes) throws IOException {
        // ItemForm에서 MultipartFile 타입으로 들어간 attachFile을 가져오고
        // storeFile 메소드에 넣어준다. 그러면 UploadFile에
        // 사용자가 등록한 파일명과 서버에서 관리하는 파일명 두 개가 등록이 된다.
        UploadFile attachFile = fileStore.storeFile(form.getAttachFile());
        // FileStore에 ItemForm List<MultipartFile>로 이미지를 여러개 넣은 것을
        // 넣어준다. 그리고 그것을 List<UploadFile>에 넣어준다.
        List<UploadFile> storeImageFiles = fileStore.storeFiles(form.getImageFiles());

        //데이터베이스에 저장
        // 파일 이름(사용자가 등록할 때 적을 이름), 파일 등록할 때 이름, 이미지 파일들을 Item에 넣어줌
        Item item = new Item(form.getItemName(), attachFile, storeImageFiles);
        // JPA에 있는 save 메소드를 사용해서 DB에 저장
        itemRepository.save(item);

        // return이 redirect이기 때문에 redirectAttributes를 사용해서 값을 넣어줌
        redirectAttributes.addAttribute("itemId", item.getId());

        return "redirect:/items/{itemId}";
    }

    // Post형식인 saveItem메소드가 return을 /items/{itemId}로 바로 던져줘서
    // @GetMapping("/items/{id}")을 통해 가져와서 item.getId()에 해당하는
    // 페이지를 보여준다.
    @GetMapping("/items/{id}")
    public String items(@PathVariable Long id, Model model) {
        Item item = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("NOT FOUND ITEM :" + id));
        model.addAttribute("item", item);
        return "item-view";
    }


    // 이미지가 보이게 하려면 이 메소드 작성
    @ResponseBody
    @GetMapping("/images/{filename}")
    public Resource downloadImage(@PathVariable String filename) throws MalformedURLException {
        // "file:C:/upload/file/xxxxxxxx.png" 이런식으로 되는데
        // 여기서 x는 파일마다 다르므로 임시로 x라고 표시함
        // 그러면 UrlResource가 찾아온다.
        return new UrlResource("file:" + fileStore.getFullPath(filename));
    }

    // 첨부 파일 다운로드
    @GetMapping("/attach/{itemId}")
    public ResponseEntity<Resource> downloadAttach(@PathVariable Long itemId) throws MalformedURLException {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("NOT FOUND ITEM :" + itemId));
        String storeFileName = item.getAttachFile().getStoreFileName();
        String uploadFileName = item.getAttachFile().getUploadFileName();

        UrlResource resource = new UrlResource("file:" + fileStore.getFullPath(storeFileName));

        log.info("uploadFileName={}", uploadFileName);
        String encodedUploadFileName = UriUtils.encode(uploadFileName, StandardCharsets.UTF_8);
        String contentDisposition = "attachment; filename=\"" + encodedUploadFileName + "\"";
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition).body(resource);
    }
}
