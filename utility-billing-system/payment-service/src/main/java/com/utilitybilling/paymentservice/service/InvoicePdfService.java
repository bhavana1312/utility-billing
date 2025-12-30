package com.utilitybilling.paymentservice.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.utilitybilling.paymentservice.dto.InvoicePdfData;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class InvoicePdfService {

	public byte[] generate(InvoicePdfData d) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Document doc = new Document(PageSize.A4, 40, 40, 40, 40);

		try {
			PdfWriter.getInstance(doc, out);
			doc.open();

			Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
			Font header = new Font(Font.HELVETICA, 12, Font.BOLD);
			Font body = new Font(Font.HELVETICA, 11);

			Paragraph p = new Paragraph("UTILITY BILL INVOICE\n\n", title);
			p.setAlignment(Element.ALIGN_CENTER);
			doc.add(p);

			doc.add(section("Invoice Details", header));
			doc.add(kv("Invoice ID", d.getInvoiceId(), body));
			doc.add(kv("Utility Type", d.getUtilityType(), body));
			doc.add(kv("Meter Number", d.getMeterNumber(), body));
			doc.add(kv("Consumer ID", d.getConsumerId(), body));
			doc.add(kv("Email", d.getEmail(), body));
			doc.add(Chunk.NEWLINE);

			doc.add(section("Meter Readings", header));
			doc.add(kv("Previous Reading", String.valueOf(d.getPreviousReading()), body));
			doc.add(kv("Current Reading", String.valueOf(d.getCurrentReading()), body));
			doc.add(kv("Units Consumed", String.valueOf(d.getUnitsConsumed()), body));
			doc.add(Chunk.NEWLINE);

			doc.add(section("Charges Breakdown", header));
			doc.add(kv("Energy Charge", "₹" + d.getEnergyCharge(), body));
			doc.add(kv("Fixed Charge", "₹" + d.getFixedCharge(), body));
			doc.add(kv("Tax Amount", "₹" + d.getTaxAmount(), body));
			doc.add(kv("Penalty", "₹" + d.getPenaltyAmount(), body));
			doc.add(Chunk.NEWLINE);

			doc.add(section("Total Amount", header));
			doc.add(kv("Total Payable", "₹" + d.getTotalAmount(), body));
			doc.add(Chunk.NEWLINE);

			doc.add(section("Dates", header));
			doc.add(kv("Bill Generated At", String.valueOf(d.getBillGeneratedAt()), body));
			doc.add(kv("Bill Due Date", String.valueOf(d.getBillDueDate()), body));
			doc.add(kv("Payment Date", String.valueOf(d.getPaymentDate()), body));

			doc.close();
		} catch (Exception e) {
			throw new RuntimeException("PDF generation failed", e);
		}

		return out.toByteArray();
	}

	private Paragraph section(String title, Font font) {
		Paragraph p = new Paragraph(title + "\n", font);
		p.setSpacingAfter(8);
		return p;
	}

	private Paragraph kv(String k, String v, Font f) {
		return new Paragraph(k + " : " + v, f);
	}
}
