package com.kh.flokrGroupware.attachment.model.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class Attachment {
	
	private int attachmentNo;
	private String refType;
	private int refNo;
	private int uploaderEmpNo;
	private String originalFilename;
	private String storedFilepath;
	private String fileExtension;
	private String uploadDate;
	private String status;

}
