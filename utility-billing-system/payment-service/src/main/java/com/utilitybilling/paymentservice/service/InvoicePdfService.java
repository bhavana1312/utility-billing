package com.utilitybilling.paymentservice.service;

import java.io.ByteArrayOutputStream;

import org.springframework.stereotype.Service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.utilitybilling.paymentservice.dto.InvoicePdfData;
import com.utilitybilling.paymentservice.exception.InvoicePdfGenerationException;
import com.utilitybilling.paymentservice.feign.ConsumerClient;
import com.utilitybilling.paymentservice.feign.ConsumerResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

	private final ConsumerClient consumerClient;

	public byte[] generate(InvoicePdfData d) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document doc = new Document(PageSize.A4, 40, 40, 40, 40);

		ConsumerResponse consumer = consumerClient.get(d.getConsumerId());

		try {
			PdfWriter.getInstance(doc, out);
			doc.open();

			Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
			Font section = new Font(Font.HELVETICA, 12, Font.BOLD);
			Font header = new Font(Font.HELVETICA, 11, Font.BOLD);
			Font body = new Font(Font.HELVETICA, 11);

			Paragraph heading = new Paragraph("UTILITY BILL INVOICE\n\n", title);
			heading.setAlignment(Element.ALIGN_CENTER);
			doc.add(heading);

			doc.add(section("Invoice Details", section));
			PdfPTable info = table(2);
			add(info, "Invoice ID", d.getInvoiceId(), header, body);
			add(info, "Utility Type", d.getUtilityType(), header, body);
			add(info, "Consumer ID", d.getConsumerId(), header, body);
			add(info, "Meter Number", d.getMeterNumber(), header, body);
			add(info, "Email", consumer.getEmail(), header, body);
			doc.add(info);

			doc.add(space());

			doc.add(section("Meter Readings", section));
			PdfPTable readings = table(2);
			add(readings, "Previous Reading", String.valueOf(d.getPreviousReading()), header, body);
			add(readings, "Current Reading", String.valueOf(d.getCurrentReading()), header, body);
			add(readings, "Units Consumed", String.valueOf(d.getUnitsConsumed()), header, body);
			doc.add(readings);

			doc.add(space());

			doc.add(section("Charges Breakdown", section));
			PdfPTable charges = table(2);
			add(charges, "Energy Charge", "Rs." + d.getEnergyCharge(), header, body);
			add(charges, "Fixed Charge", "Rs." + d.getFixedCharge(), header, body);
			add(charges, "Tax Amount", "Rs." + d.getTaxAmount(), header, body);
			add(charges, "Penalty", "Rs." + d.getPenaltyAmount(), header, body);
			doc.add(charges);

			doc.add(space());

			PdfPTable total = table(2);
			PdfPCell l = new PdfPCell(new Phrase("Total Payable", header));
			PdfPCell v = new PdfPCell(new Phrase("Rs." + d.getTotalAmount(), header));
			l.setBorder(Rectangle.NO_BORDER);
			v.setBorder(Rectangle.NO_BORDER);
			l.setPadding(8);
			v.setPadding(8);
			v.setHorizontalAlignment(Element.ALIGN_RIGHT);
			total.addCell(l);
			total.addCell(v);
			doc.add(total);

			doc.add(space());

			doc.add(section("Dates", section));
			PdfPTable dates = table(2);
			add(dates, "Bill Generated At", String.valueOf(d.getBillGeneratedAt()), header, body);
			add(dates, "Bill Due Date", String.valueOf(d.getBillDueDate()), header, body);
			add(dates, "Payment Date", String.valueOf(d.getPaymentDate()), header, body);
			doc.add(dates);

			doc.close();

		} catch (Exception e) {
			throw new InvoicePdfGenerationException("PDF rendering error", e);
		}

		return out.toByteArray();
	}

	private Paragraph section(String t, Font f) {
		Paragraph p = new Paragraph(t + "\n", f);
		p.setSpacingAfter(6);
		return p;
	}

	private PdfPTable table(int c) {
		PdfPTable t = new PdfPTable(c);
		t.setWidthPercentage(100);
		return t;
	}

	private void add(PdfPTable t, String k, String v, Font h, Font b) {
		PdfPCell c1 = new PdfPCell(new Phrase(k, h));
		PdfPCell c2 = new PdfPCell(new Phrase(v, b));
		c1.setPadding(6);
		c2.setPadding(6);
		t.addCell(c1);
		t.addCell(c2);
	}

	private Paragraph space() {
		Paragraph p = new Paragraph(" ");
		p.setSpacingAfter(10);
		return p;
	}
}
