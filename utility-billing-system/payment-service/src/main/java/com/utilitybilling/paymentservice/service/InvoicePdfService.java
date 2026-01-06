package com.utilitybilling.paymentservice.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.utilitybilling.paymentservice.dto.InvoicePdfData;
import com.utilitybilling.paymentservice.exception.InvoicePdfGenerationException;
import com.utilitybilling.paymentservice.feign.ConsumerClient;
import com.utilitybilling.paymentservice.feign.ConsumerResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

	private final ConsumerClient consumerClient;

	public byte[] generate(InvoicePdfData d) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document doc = new Document(PageSize.A4, 36, 36, 36, 36);

		System.out.print(d);

		ConsumerResponse consumer = consumerClient.get(d.getConsumerId());

		try {
			PdfWriter.getInstance(doc, out);
			doc.open();

			Font title = new Font(Font.HELVETICA, 20, Font.BOLD);
			Font subtitle = new Font(Font.HELVETICA, 12, Font.BOLD);
			Font header = new Font(Font.HELVETICA, 11, Font.BOLD);
			Font body = new Font(Font.HELVETICA, 11);
			Font small = new Font(Font.HELVETICA, 10);

			addHeader(doc, title);
			addInvoiceMeta(doc, subtitle, body, d);
			addBillingInfo(doc, header, body, consumer, d);
			addMeterSection(doc, header, body, d);
			addChargesTable(doc, header, body, d);
			addTotalBox(doc, header, d);
			addDates(doc, header, body, d);
			addFooter(doc, small);

			doc.close();
			return out.toByteArray();

		} catch (Exception e) {
			throw new InvoicePdfGenerationException("PDF rendering error", e);
		}
	}

	private void addHeader(Document doc, Font title) throws Exception {
		Paragraph p = new Paragraph("UTILITY BILL INVOICE", title);
		p.setAlignment(Element.ALIGN_CENTER);
		p.setSpacingAfter(20);
		doc.add(p);
	}

	private void addInvoiceMeta(Document doc, Font subtitle, Font body, InvoicePdfData d) throws Exception {
		PdfPTable t = new PdfPTable(2);
		t.setWidthPercentage(100);
		t.setSpacingAfter(15);
		t.setWidths(new float[] { 70, 30 });

		PdfPCell left = new PdfPCell();
		left.setBorder(Rectangle.NO_BORDER);
		left.addElement(new Paragraph("Utility Billing System", subtitle));
		left.addElement(new Paragraph("Official Payment Invoice", body));

		PdfPCell right = new PdfPCell();
		right.setBorder(Rectangle.NO_BORDER);
		right.setHorizontalAlignment(Element.ALIGN_RIGHT);
		right.addElement(new Paragraph("Invoice ID: " + d.getInvoiceId(), body));
		right.addElement(new Paragraph("Utility: " + d.getUtilityType(), body));

		t.addCell(left);
		t.addCell(right);
		doc.add(t);
	}

	private void addBillingInfo(Document doc, Font header, Font body, ConsumerResponse c, InvoicePdfData d)
			throws Exception {
		PdfPTable t = new PdfPTable(2);
		t.setWidthPercentage(100);
		t.setSpacingAfter(20);
		t.setWidths(new float[] { 50, 50 });

		t.addCell(sectionCell("Billed To", header));
		t.addCell(sectionCell("Connection Details", header));

		t.addCell(valueCell(c.getEmail(), body));
		t.addCell(valueCell("Meter No: " + d.getMeterNumber(), body));

		t.addCell(valueCell("Consumer ID: " + d.getConsumerId(), body));
		t.addCell(valueCell("Utility Type: " + d.getUtilityType(), body));

		doc.add(t);
	}

	private void addMeterSection(Document doc, Font header, Font body, InvoicePdfData d) throws Exception {
		PdfPTable t = new PdfPTable(3);
		t.setWidthPercentage(100);
		t.setSpacingAfter(20);

		t.addCell(headerCell("Previous Reading", header));
		t.addCell(headerCell("Current Reading", header));
		t.addCell(headerCell("Units Consumed", header));

		t.addCell(valueCell(String.valueOf(d.getPreviousReading()), body));
		t.addCell(valueCell(String.valueOf(d.getCurrentReading()), body));
		t.addCell(valueCell(String.valueOf(d.getUnitsConsumed()), body));

		doc.add(t);
	}

	private void addChargesTable(Document doc, Font header, Font body, InvoicePdfData d) throws Exception {
		PdfPTable t = new PdfPTable(2);
		t.setWidthPercentage(75);
		t.setHorizontalAlignment(Element.ALIGN_CENTER);
		t.setSpacingAfter(20);
		t.setWidths(new float[] { 70, 30 });

		t.addCell(headerCell("Charge Description", header));
		t.addCell(amountHeaderCell("Amount (₹)", header));

		addRow(t, "Energy Charge", d.getEnergyCharge(), body);
		addRow(t, "Fixed Charge", d.getFixedCharge(), body);
		addRow(t, "Tax", d.getTaxAmount(), body);
		addRow(t, "Penalty", d.getPenaltyAmount(), body);

		doc.add(t);
	}

	private void addTotalBox(Document doc, Font header, InvoicePdfData d) throws Exception {
		PdfPTable t = new PdfPTable(2);
		t.setWidthPercentage(75);
		t.setHorizontalAlignment(Element.ALIGN_CENTER);
		t.setSpacingAfter(25);
		t.setWidths(new float[] { 70, 30 });

		PdfPCell l = new PdfPCell(new Phrase("TOTAL PAYABLE", header));
		PdfPCell r = new PdfPCell(new Phrase("₹ " + d.getTotalAmount(), header));

		l.setPadding(12);
		r.setPadding(12);
		r.setHorizontalAlignment(Element.ALIGN_RIGHT);

		t.addCell(l);
		t.addCell(r);
		doc.add(t);
	}

	private void addDates(Document doc, Font header, Font body, InvoicePdfData d) throws Exception {
		PdfPTable t = new PdfPTable(2);
		t.setWidthPercentage(60);
		t.setHorizontalAlignment(Element.ALIGN_CENTER);
		t.setSpacingAfter(20);

		t.addCell(headerCell("Bill Generated", header));
		t.addCell(valueCell(formatInstant(d.getBillGeneratedAt()), body));

		t.addCell(headerCell("Due Date", header));
		t.addCell(valueCell(formatInstant(d.getBillDueDate()), body));

		t.addCell(headerCell("Payment Date", header));
		t.addCell(valueCell(formatInstant(d.getPaymentDate()), body));

		doc.add(t);
	}

	private void addFooter(Document doc, Font small) throws Exception {
		Paragraph p = new Paragraph("This is a system-generated invoice. No signature required.", small);
		p.setAlignment(Element.ALIGN_CENTER);
		p.setSpacingBefore(30);
		doc.add(p);
	}

	private PdfPCell headerCell(String text, Font f) {
		PdfPCell c = new PdfPCell(new Phrase(text, f));
		c.setPadding(8);
		return c;
	}

	private PdfPCell amountHeaderCell(String text, Font f) {
		PdfPCell c = new PdfPCell(new Phrase(text, f));
		c.setPadding(8);
		c.setHorizontalAlignment(Element.ALIGN_RIGHT);
		return c;
	}

	private PdfPCell valueCell(String text, Font f) {
		PdfPCell c = new PdfPCell(new Phrase(text, f));
		c.setPadding(8);
		return c;
	}

	private PdfPCell sectionCell(String text, Font f) {
		PdfPCell c = new PdfPCell(new Phrase(text, f));
		c.setPadding(8);
		c.setBackgroundColor(Color.LIGHT_GRAY);
		return c;
	}

	private void addRow(PdfPTable t, String label, Object value, Font body) {
		t.addCell(valueCell(label, body));
		PdfPCell v = valueCell(String.valueOf(value), body);
		v.setHorizontalAlignment(Element.ALIGN_RIGHT);
		t.addCell(v);
	}

	private String formatInstant(Object o) {
		if (o == null)
			return "-";
		return ZonedDateTime.ofInstant((Instant) o, ZoneId.systemDefault())
				.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"));
	}
}
