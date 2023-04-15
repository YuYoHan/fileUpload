package com.example.fileupload.file;

import com.example.fileupload.domain.UploadFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
// 멀티파트 파일을 서버에 저장하는 역할을 한다.
public class FileStore {
    @Value("${file.dir}")
    private String fileDir;

    public String getFullPath(String filename) {
        return fileDir + filename;
    }

    // 여러개의 파일을 담을 때
    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();
        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile));
            }
        }
        return storeFileResult;
    }

    // MultipartFile을 가지고 파일을 저장한 다음 UploadFile로 반환해준다.
    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        // 사용자가 업로드한 파일 name을 가지고 온다.
        String originalFilename = multipartFile.getOriginalFilename();
        // 사용자가 업로드한 파일 이름을 createStoreFileName에 넘겨준다.
        // 예를들어, "qwe-qwe-123-qqwe.png";을 storeFilename에 담아둔다.
        String storeFilename = createStoreFileName(originalFilename);
        // "qwe-qwe-123-qqwe.png";을 넘기면 getFullPath 메소드에서
        // "c:/upload/file/qwe-qwe-123-qqwe.png";이렇게 합쳐진다.
        multipartFile.transferTo(new File(getFullPath(storeFilename)));
        // 사용자가 업로드한 파일 이름과 "qwe-qwe-123-qqwe.png";을
        // UploadFile에 보내준다.
        return new UploadFile(originalFilename, storeFilename);
    }

    // 서버 내부에서 관리하는 파일명은 유일한 이름을 생성하는 "UUID"를 사용해서 충돌되지 않도록 한다.
    private String createStoreFileName(String originalFilename) {
        // extractExt메소드를 실행해준 것을 ext에 담아준다.
        // 예를들어, extractExt 메소드에서 png을 리턴해주면 그것을 ext에 담는 것이다.
        String ext = extractExt(originalFilename);
        // uuid를 뽑으면 "qwe-qwe-123-qqwe"; 이런식으로 된다.
        String uuid = UUID.randomUUID().toString();
        // 그러면 리턴은 "qwe-qwe-123-qqwe.png"; 이런식으로 된다.
        return uuid + "." + ext;
    }

    // 확장자를 별도로 추출해서 서버 내부에서 관리하는 파일명에도 붙여준다.
    private String extractExt(String originalFilename) {
        // 위치를 가져온다. (image.png 여기서 .을 의미합니다.)
        int pos = originalFilename.lastIndexOf(".");
        // image.png에서 .다음 png를 뽑을 수 있습니다.
        return originalFilename.substring(pos + 1);
    }
}
