package com.dguntha.personalapis.utils;

import com.dguntha.personalapis.model.dto.MetaDataDto;
import org.bson.Document;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DocumentCompare {

    public static void analyticsOCRDetails(Document document) {
        int matchingCount = 0;
        int notMatchingCount = 0;
        int totalFields = 0;

        if (document.containsKey("attachments") && (document.get("attachments") instanceof LinkedHashMap)) {
            LinkedHashMap<String,Object> attachmentsObj = (LinkedHashMap) document.get("attachments");
            if (attachmentsObj.containsKey("ocr_metadata") && attachmentsObj.get("ocr_metadata") instanceof ArrayList) {
                ArrayList ocrMetaDatas = (ArrayList) attachmentsObj.get("ocr_metadata");


                for (Object attachmentObj : ocrMetaDatas) {
                    ++totalFields;
                    if (attachmentObj instanceof LinkedHashMap) {
                        LinkedHashMap attachment =  (LinkedHashMap) attachmentObj;


                        if (attachment.containsKey("ocrFields") && attachment.get("ocrFields") instanceof LinkedHashMap) {
                            LinkedHashMap ocrField = (LinkedHashMap) attachment.get("ocrFields");
                            if (ocrField != null) {
                                Object ocrTextObj = ocrField.get("text");
                                Object reviewedTextObj = ocrField.get("original_ocr_text");
                                if (ocrTextObj != null && reviewedTextObj != null && ocrTextObj.equals(reviewedTextObj)) {
                                    matchingCount++;
                                } else {
                                    notMatchingCount++;
                                }
                            }
                        }
                    } else if (attachmentObj instanceof MetaDataDto) {
                        LinkedHashMap attachment = ((LinkedHashMap) ((MetaDataDto)attachmentObj).getOcrFields());
                        if (attachment != null) {
                            Object ocrTextObj = attachment.get("text");
                            Object reviewedTextObj = attachment.get("original_ocr_text");
                            if (ocrTextObj != null && reviewedTextObj != null && ocrTextObj.equals(reviewedTextObj)) {
                                matchingCount++;
                            } else {
                                notMatchingCount++;
                            }
                        }
                    }
                }
            }
        }

        int x = matchingCount == 0 ? 0 : ((matchingCount * 100 / totalFields));
        Document analytic = new Document("matchCount", matchingCount)
                .append("notMatchCount", notMatchingCount)
                .append("totalFields", totalFields)
                .append("orcNotFound", (totalFields - (matchingCount + notMatchingCount)))
                .append("percentage", x);
        document.append("ocrAccuracy", analytic);
    }
}
