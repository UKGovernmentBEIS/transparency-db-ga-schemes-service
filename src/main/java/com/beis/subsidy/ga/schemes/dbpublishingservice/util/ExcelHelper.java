package com.beis.subsidy.ga.schemes.dbpublishingservice.util;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.AuditLogs;
import com.beis.subsidy.ga.schemes.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.ga.schemes.dbpublishingservice.repository.AuditLogsRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.expression.ParseException;
import org.springframework.web.multipart.MultipartFile;

import com.beis.subsidy.ga.schemes.dbpublishingservice.model.BulkUploadSchemes;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExcelHelper {

    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public final static int EXPECTED_COLUMN_COUNT = 16;

    public  final static String SHEET = "Upload Template";

    /**
     * Method to check excel file format to be xlsx
     * @param file
     * @return
     */
    public static boolean hasExcelFormat(MultipartFile file) {
        boolean flag = true;
        if(!TYPE.equals(file.getContentType())) {
            flag = false;
        }
        log.info("Inside ExcelHelper hasExcelFormat ::{}", flag);
        return flag;
    }



    public static List<BulkUploadSchemes> excelToSchemes(InputStream is) {
        try {

            log.info("Inside excelToAwards::DBPublishingSubsidies Service" );

            Workbook workbook = new XSSFWorkbook(is);

            Sheet sheet = workbook.getSheet(SHEET);
            Iterator<Row> rows = sheet.iterator();

            log.info("first row " + sheet.getFirstRowNum());
            List<BulkUploadSchemes> BulkUploadSchemesList = new ArrayList<BulkUploadSchemes>();
            log.info("last row " + sheet.getLastRowNum());
            int rowNumber = 0;
            while (rows.hasNext()) {
                log.info("before rows.next");
                Row currentRow = rows.next();


                // skip header
                if (rowNumber == 0) {
                    rowNumber++;
                    continue;
                }
                if (containsValue(currentRow)) {
                    log.info("BulkUploadSchemesController Going Inside switch block" ,rowNumber);

                    BulkUploadSchemes bulkUploadSchemes = new BulkUploadSchemes();
                    bulkUploadSchemes.setRow(currentRow.getRowNum() + 1);

                    for (int i = 0; i < currentRow.getLastCellNum(); i++){
                        Cell currentCell = currentRow.getCell(i, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                        switch (i) {
                            case 0:
                                if (currentCell.getCellType() == CellType.BLANK) {
                                    bulkUploadSchemes.setPublicAuthorityName(null);
                                } else {
                                    bulkUploadSchemes.setPublicAuthorityName(currentCell.getStringCellValue().trim());
                                }
                                break;

                            case 1:
                                if (currentCell.getCellType() == CellType.BLANK) {
                                    bulkUploadSchemes.setSubsidySchemeName(null);
                                } else {
                                    bulkUploadSchemes.setSubsidySchemeName(currentCell.getStringCellValue().trim());
                                }

                                break;

                            case 2:
                                if (currentCell.getCellType() == CellType.BLANK){
                                    bulkUploadSchemes.setSubsidySchemeInterest(null);
                                } else {
                                    bulkUploadSchemes.setSubsidySchemeInterest(currentCell.getStringCellValue());
                                }
                                break;
                            case 3:
                                if (currentCell.getCellType() == CellType.BLANK) {
                                    bulkUploadSchemes.setSpecificPolicyObjective(null);
                                } else {
                                    bulkUploadSchemes.setSpecificPolicyObjective(currentCell.getStringCellValue().trim());
                                }
                                break;

                            case 4:
                                if (currentCell.getCellType() == CellType.BLANK) {
                                    bulkUploadSchemes.setSubsidySchemeDescription(null);
                                } else {
                                    bulkUploadSchemes.setSubsidySchemeDescription(currentCell.getStringCellValue().trim());
                                }
                                break;

                            case 5:
                                if (currentCell.getCellType() == CellType.BLANK) {
                                    bulkUploadSchemes.setLegalBasis(null);
                                } else {
                                    bulkUploadSchemes.setLegalBasis(currentCell.getStringCellValue().trim());
                                }
                                break;

                            case 6:
                                if (currentCell.getCellType() == CellType.BLANK) {
                                    bulkUploadSchemes.setPublicAuthorityPolicyURL(null);
                                } else {
                                    bulkUploadSchemes.setPublicAuthorityPolicyURL(currentCell.getStringCellValue().trim());
                                }

                                break;

                            case 7:
                                if (currentCell.getCellType() == CellType.BLANK) {
                                    bulkUploadSchemes.setPublicAuthorityPolicyPageDescription(null);
                                } else {
                                    bulkUploadSchemes.setPublicAuthorityPolicyPageDescription(currentCell.getStringCellValue().trim());
                                }

                                break;

                            case 8:
                                if (currentCell.getCellType() == CellType.BLANK) {
                                    bulkUploadSchemes.setBudget(null);
                                }
                                if (currentCell.getCellType() == CellType.STRING) {
                                    bulkUploadSchemes.setBudget("Invalid");
                                }
                                if (currentCell.getCellType() == CellType.NUMERIC) {
                                    bulkUploadSchemes.setBudget((String.valueOf(currentCell.getNumericCellValue())));
                                }

                                break;

                            case 9:
                                if (currentCell.getCellType() != CellType.BLANK) {
                                    bulkUploadSchemes.setMaximumAmountGivenUnderScheme(String.valueOf(currentCell).trim());
                                } else {
                                    bulkUploadSchemes.setMaximumAmountGivenUnderScheme(null);
                                }

                                break;

                            case 10:
                                if (currentCell.getCellType() == CellType.BLANK) {
                                    bulkUploadSchemes.setConfirmationDate(null);
                                }
                                if (currentCell.getCellType() == CellType.STRING) {
                                    bulkUploadSchemes.setConfirmationDate(null);

                                } else {
                                    bulkUploadSchemes.setConfirmationDate(convertDateToLocalDate(currentCell.getDateCellValue()));
                                }
                                break;

                            case 11:
                                if(currentCell.getCellType()==CellType.BLANK) {
                                    bulkUploadSchemes.setStartDate(null);
                                }
                                if(currentCell.getCellType()==CellType.STRING) {
                                    bulkUploadSchemes.setStartDate(null);

                                }
                                else {
                                    bulkUploadSchemes.setStartDate(convertDateToLocalDate(currentCell.getDateCellValue()));
                                }

                                break;

                            case 12:
                                if(currentCell.getCellType()==CellType.BLANK || StringUtils.isEmpty(String.valueOf(currentCell))) {
                                    bulkUploadSchemes.setEndDate(null);
                                    bulkUploadSchemes.setHasNoEndDate(true);

                                } else if (currentCell.getCellType()==CellType.STRING) {
                                    DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                                    bulkUploadSchemes.setEndDate(LocalDate.parse("01-01-0001", DATE_FORMAT));

                                } else {
                                    bulkUploadSchemes.setEndDate(convertDateToLocalDate(currentCell.getDateCellValue()));
                                }

                                break;

                            case 13:
                                if(currentCell.getCellType()==CellType.BLANK) {
                                    bulkUploadSchemes.setSpendingSectors(null);
                                } else {
                                    bulkUploadSchemes.setSpendingSectors(currentCell.getStringCellValue().trim());
                                }

                                break;

                            case 14:
                                if(currentCell.getCellType()==CellType.BLANK) {
                                    bulkUploadSchemes.setPurpose(null);
                                } else {
                                    bulkUploadSchemes.setPurpose(currentCell.getStringCellValue().trim());
                                }

                                break;

                            case 15:
                                //if purpose and other purpose are both blank - sets them both to null
                                if(currentCell.getCellType()==CellType.BLANK && bulkUploadSchemes.getPurpose() == null) {
                                    bulkUploadSchemes.setPurposeOther(null);
                                    //if purpose other is populated but purpose is blank
                                }else if(currentCell.getCellType()!=CellType.BLANK && bulkUploadSchemes.getPurpose() == null) {
                                    bulkUploadSchemes.setPurpose("Other - " + currentCell.getStringCellValue().trim());
                                    bulkUploadSchemes.setPurposeOther(currentCell.getStringCellValue().trim());
                                    //if purpose and other purpose are both populated
                                }else {
                                    bulkUploadSchemes.setPurposeOther(currentCell.getStringCellValue().trim());
                                    bulkUploadSchemes.setPurpose(bulkUploadSchemes.getPurpose() + " | Other - " + currentCell.getStringCellValue().trim());
                                }

                                break;

                            default:
                                break;
                        }
                    }

                    BulkUploadSchemesList.add(bulkUploadSchemes);
                } else {
                    break;
                }
            }

            workbook.close();

            log.info("Excel - List - size = " + BulkUploadSchemesList.size());
            return BulkUploadSchemesList;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        catch (Exception e) {

            throw new RuntimeException("fail to read Excel file: " + e);
        }
    }

    private static String convertDateToString(Date incomingDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);
        String date = null ;
        if(incomingDate!=null) {
            try {
                date = formatter.format(incomingDate).toString();
            } catch (ParseException e) {
                log.error("Error while converting the date inside convertDateToString",e);
                return date;
            }
        }
        return date;
    }

    private static LocalDate convertDateToLocalDate(Date incomingDate){
        return incomingDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static boolean containsValue(Row row){
        boolean flag = true;
        Row.MissingCellPolicy policy = Row.MissingCellPolicy.CREATE_NULL_AS_BLANK; // If the Cell returned doesn't exist, instead of returning null, create a new Cell with a cell type of "blank".

        if(row.getCell(0, policy).getCellType()==CellType.BLANK && row.getCell(1, policy).getCellType()==CellType.BLANK &&
                row.getCell(2, policy).getCellType()==CellType.BLANK) {
            return false;
        }
        if ((StringUtils.isEmpty(String.valueOf(row.getCell(0, policy))) &&
                StringUtils.isEmpty(String.valueOf(row.getCell(1, policy))) &&
                StringUtils.isEmpty(String.valueOf(row.getCell(2, policy)))) ||
                (String.valueOf(row.getCell(0, policy))==null && String.valueOf(row.getCell(1, policy))==null &&
                        String.valueOf(row.getCell(2, policy))==null ) && row.getCell(2, policy).getCellType()==CellType.BLANK){
            flag = false;
        }
        return flag;
    }

    public static void saveBulkUploadSchemesAuditLog(List<SubsidyMeasure> savedSchemes, AuditLogsRepository auditLogsRepository) {
        List<AuditLogs> auditLogs = savedSchemes.stream().map(scheme -> {
            AuditLogs auditLog = new AuditLogs();
            auditLog.setUserName(scheme.getCreatedBy());
            auditLog.setGaName(scheme.getGrantingAuthority().getGrantingAuthorityName());
            auditLog.setEventType("Bulk upload scheme(s)");
            auditLog.setEventId(scheme.getScNumber());
            auditLog.setEventMessage("Scheme " + scheme.getScNumber() + " bulk uploaded.");
            return auditLog;
        }).collect(Collectors.toList());

        saveBulkUploadSchemeAuditMessage(savedSchemes, auditLogs, auditLogsRepository);
    }

    public static void saveBulkUploadSchemeAuditMessage (List<?> savedAwardsList, List<AuditLogs> auditLogs, AuditLogsRepository auditLogsRepository){
        log.info("creating summary scheme bulk upload message");
        String msg = bulkUploadAuditMsgBuilder(savedAwardsList,auditLogs);
        AuditLogs audit = new AuditLogs();
        audit.setUserName(auditLogs.get(0).getUserName());
        audit.setEventType(auditLogs.get(0).getEventType());
        audit.setEventId(auditLogs.get(0).getEventId());
        audit.setEventMessage(msg);
        audit.setGaName(auditLogs.get(0).getGaName());
        audit.setCreatedTimestamp(LocalDateTime.now());
        auditLogsRepository.save(audit);
    }

    public static String bulkUploadAuditMsgBuilder (List<?> savedSchemesList, List<AuditLogs> auditLogs){
        String msg = "";

        if(savedSchemesList == null || auditLogs == null || savedSchemesList.isEmpty()){
            log.error("Error in bulkUploadAuditMsgBuilder :: savedAwardsList: {} auditLogs: {}", savedSchemesList, auditLogs);
            return msg;
        }

        List<SubsidyMeasure> savedSchemes = (List<SubsidyMeasure>) savedSchemesList;
        msg = String.format(
                "%d Scheme(s) (%s-%s) bulk uploaded successfully",
                auditLogs.size(),
                savedSchemes.get(0).getScNumber(),
                savedSchemes.get(savedSchemes.size() - 1).getScNumber()
        );
        return msg;
    }
    public static void saveAuditLogForUpdate(UserPrinciple userPrinciple, String action,String awardNo, String eventMsg,
                                             AuditLogsRepository auditLogsRepository) {
        AuditLogs audit = new AuditLogs();
        try {
            String userName = userPrinciple.getUserName();
            audit.setUserName(userName);
            audit.setEventType(action);
            audit.setEventId(awardNo);
            audit.setEventMessage(eventMsg.toString());
            audit.setGaName(userPrinciple.getGrantingAuthorityGroupName());
            audit.setCreatedTimestamp(LocalDateTime.now());
            auditLogsRepository.save(audit);
        } catch(Exception e) {
            log.error("{} :: saveAuditLogForUpdate failed to perform action", e);
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static Boolean validateColumnCount(InputStream is) {
        Workbook workbook = null;
        try {
            workbook = new XSSFWorkbook(is);
            Sheet sheet = workbook.getSheet(SHEET);

            int headerColumnCount = sheet.getRow(0).getLastCellNum();
            if (headerColumnCount == EXPECTED_COLUMN_COUNT){
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException("fail to read Excel file: " + e);
        }
        return false;
    }
}