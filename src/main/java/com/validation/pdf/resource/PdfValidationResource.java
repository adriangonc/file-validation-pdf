package com.validation.pdf.resource;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.itextpdf.text.exceptions.InvalidPdfException;
import com.itextpdf.text.io.RandomAccessSource;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PdfDictionary;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;

@RestController
@RequestMapping("/api/pdf/validation")
public class PdfValidationResource {

	@PostMapping
	public ResponseEntity<Object> validatePdf(@RequestParam("file") MultipartFile file) {
		try {
			if (thereAreErrorsInPdf(multipartToFile(file, "pdf")) == false) {
				return new ResponseEntity<Object>(HttpStatus.OK);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Object>("Invalid file!", HttpStatus.BAD_REQUEST);

	}

	public static boolean thereAreErrorsInPdf(File file) throws IOException {
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			RandomAccessSource ras = new RandomAccessSourceFactory().createSource(raf);
			RandomAccessFileOrArray rafoa = new RandomAccessFileOrArray(ras);
			PdfReader pdf = new PdfReader(rafoa, new byte[0]);
			if (pdf.isRebuilt()) {
				return true;
			}
			pdf.close();
			return false;
		} catch (InvalidPdfException ipe) {
			return true;
		}
	}

	public static List<String> listPdfFonts(PdfReader reader) throws IOException {
		List<String> fonts = new ArrayList<String>();
		int n = reader.getXrefSize();
		PdfObject object;
		PdfDictionary font;
		for (int i = 0; i < n; i++) {
			object = reader.getPdfObject(i);
			if (object == null || !object.isDictionary()) {
				continue;
			}

			font = (PdfDictionary) object;

			if (font.get(PdfName.FONTNAME) != null) {
				System.out.println("fontn	ame " + font.get(PdfName.FONTNAME));
				fonts.add(font.get(PdfName.FONTNAME).toString());
			}
		}
		return fonts;
	}
	
	public static File multipartToFile(MultipartFile multipart, String fileName) throws IllegalStateException, IOException {
	    File convFile = new File(System.getProperty("java.io.tmpdir")+"/"+fileName);
	    multipart.transferTo(convFile);
	    return convFile;
	}

}
