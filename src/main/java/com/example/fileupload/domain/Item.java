package com.example.fileupload.domain;

import lombok.*;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class Item {

    @Id
    @GeneratedValue
    private Long id;
    private String itemName;

    // 가독성을 위해서 사용합니다.
    // UploadFile라는 객체로 묶어 둔 것입니다.
    /*
    *       private String uploadFileName;
            private String storeFileName;
            이렇게 두 개가 UploadFile입니다.
    * */
    // UploadFile이란 것을 여기에 일일히 사용한다면
    // 더러워지니 @Embedded로 쓰고 묶어둔 곳은
    // @Embeddable을 써서 표시를 해둡니다.
    @Embedded
    @AttributeOverrides({
            // @Embedded를 이용해 객체로 Entity의 Column을 표현한다면,
            // Column 이름이 중복되는 문제가 발생하기도 합니다.
            // JPA에서 필드의 이름이 아닌 개발자가 지정한 이름으로 column을 생성하기 위해서
            // @Column(name = "필드이름")을 사용하면 됩니다.
            // 하지만, 이는 상식적으로 생각해보았을때 해결방법이 되지 못합니다.
            // 바로 같은 객체를 사용하기 때문입니다. 따라서 객체안의 컬럼을 재정의하는 방법이 필요한데
            // 그것이 바로 @AttributeOverride입니다.
            @AttributeOverride(name = "uploadFileName", column = @Column(name = "attach_upload_file_name")),
            @AttributeOverride(name = "storeFileName", column = @Column(name = "attach_store_file_name"))
    })
    private UploadFile attachFile;


    // 값 타입 컬렉션을 매핑할 때 사용합니다.
    @ElementCollection
    // @CollectionTable은 값 타입 컬렉션을 매핑할 테이블에 대한 정보를 지정하는 역할을 수행합니다.
    //  @JoinColumn : 외래 키를 매핑할 때 사용합니다. name 속성에는 매핑할 외래 키 컬럼명(이름)을 지정합니다.
    @CollectionTable(name = "item_image", joinColumns = @JoinColumn(name = "item_id"))
    private List<UploadFile> imageFiles = new ArrayList<>();

    public Item(String itemName, UploadFile attachFile, List<UploadFile> imageFiles) {
        this.itemName = itemName;
        this.attachFile = attachFile;
        this.imageFiles = imageFiles;
    }
}