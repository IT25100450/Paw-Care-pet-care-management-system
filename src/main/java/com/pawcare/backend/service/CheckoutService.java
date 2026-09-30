package com.pawcare.backend.service;

import com.pawcare.backend.entity.Checkout;
import com.pawcare.backend.entity.CheckoutItem;
import com.pawcare.backend.repository.CheckoutRepository;

import com.pawcare.backend.dto.CheckoutItemRequest;
import com.pawcare.backend.dto.CheckoutRequest;
import com.pawcare.backend.entity.Product;
import com.pawcare.backend.exception.CheckoutException;
import com.pawcare.backend.exception.ProductNotFoundException;
import com.pawcare.backend.repository.ProductRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CheckoutService {

    private final ProductRepository productRepository;
    private final CheckoutRepository checkoutRepository;

    public CheckoutService(
            ProductRepository productRepository,
            CheckoutRepository checkoutRepository
    ) {
        this.productRepository = productRepository;
        this.checkoutRepository = checkoutRepository;
    }

    // =========================================================
    // PROCESS CHECKOUT
    // =========================================================

    @Transactional
    public byte[] processCheckout(CheckoutRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new CheckoutException("Your cart is empty.");
        }

        List<CheckoutLine> checkoutLines = new ArrayList<>();

        BigDecimal grandTotal = BigDecimal.ZERO;

        /*
         * STEP 1
         * Check every product's CURRENT database stock.
         */
        for (CheckoutItemRequest itemRequest : request.getItems()) {

            if (itemRequest.getQuantity() == null ||
                    itemRequest.getQuantity() <= 0) {

                throw new CheckoutException(
                        "Invalid quantity for product ID: "
                                + itemRequest.getProductId()
                );
            }

            Product product = productRepository
                    .findByIdForUpdate(itemRequest.getProductId())
                    .orElseThrow(() ->
                            new ProductNotFoundException(
                                    "Product not found: "
                                            + itemRequest.getProductId()
                            )
                    );

            int requestedQuantity = itemRequest.getQuantity();

            int currentStock = product.getStockQuantity();

            /*
             * Make sure customer cannot purchase
             * more than the actual database stock.
             */
            if (currentStock < requestedQuantity) {

                throw new CheckoutException(
                        "Not enough stock for "
                                + product.getProductName()
                                + ". Only "
                                + currentStock
                                + " item(s) available."
                );
            }

            BigDecimal lineTotal = product
                    .getPrice()
                    .multiply(BigDecimal.valueOf(requestedQuantity));

            checkoutLines.add(
                    new CheckoutLine(
                            product,
                            requestedQuantity,
                            lineTotal
                    )
            );

            grandTotal = grandTotal.add(lineTotal);
        }

        /*
         * STEP 2
         * Reduce stock in the DATABASE.
         */
        for (CheckoutLine line : checkoutLines) {

            Product product = line.product();

            int newStock =
                    product.getStockQuantity()
                            - line.quantity();

            product.setStockQuantity(newStock);

            productRepository.save(product);
        }

        /*
         * STEP 3
         * Save completed checkout to database.
         */
        String invoiceNumber =
                "PC-"
                        + LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyyMMddHHmmss"
                                )
                        )
                        + "-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 6)
                        .toUpperCase();

        Checkout checkout = new Checkout();

        checkout.setInvoiceNumber(invoiceNumber);
        checkout.setCustomerName(request.getCustomerName());
        checkout.setCustomerEmail(request.getCustomerEmail());
        checkout.setCustomerPhone(request.getCustomerPhone());
        checkout.setCustomerAddress(request.getCustomerAddress());
        checkout.setTotalAmount(grandTotal);
        checkout.setCheckoutDate(LocalDateTime.now());

        for (CheckoutLine line : checkoutLines) {

            CheckoutItem checkoutItem = new CheckoutItem();

            checkoutItem.setProduct(line.product());
            checkoutItem.setQuantity(line.quantity());

            /*
             * Store the product price at the time of purchase.
             */
            checkoutItem.setUnitPrice(
                    line.product().getPrice()
            );

            checkoutItem.setLineTotal(
                    line.lineTotal()
            );

            checkout.addItem(checkoutItem);
        }

        checkoutRepository.save(checkout);

        /*
         * STEP 4
         * Generate invoice PDF.
         */
        try {

            return generateInvoicePdf(
                    request,
                    checkoutLines,
                    grandTotal,
                    invoiceNumber
            );

        } catch (IOException e) {

            /*
             * Because this method is @Transactional,
             * throwing this RuntimeException causes the
             * database stock changes to roll back.
             */
            throw new CheckoutException(
                    "Unable to generate invoice. "
                            + "Checkout was cancelled."
            );
        }
    }

    // =========================================================
    // SALES DATA METHODS
    // =========================================================

    /*
     * Get every completed checkout.
     *
     * The newest checkout will appear first.
     */
    @Transactional(readOnly = true)
    public List<Checkout> getAllSales() {

        return checkoutRepository
                .findAllByOrderByCheckoutDateDesc();
    }

    /*
     * Get total revenue from all completed checkouts.
     */
    @Transactional(readOnly = true)
    public BigDecimal getTotalSales() {

        return checkoutRepository.getTotalSales();
    }

    /*
     * Get total number of completed orders.
     */
    @Transactional(readOnly = true)
    public long getTotalOrders() {

        return checkoutRepository.getTotalOrders();
    }

    /*
     * Get sales between two dates.
     */
    @Transactional(readOnly = true)
    public List<Checkout> getSalesBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {

        return checkoutRepository
                .findByCheckoutDateBetweenOrderByCheckoutDateDesc(
                        startDate,
                        endDate
                );
    }

    /*
     * Get revenue between two dates.
     */
    @Transactional(readOnly = true)
    public BigDecimal getSalesAmountBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {

        return checkoutRepository.getSalesBetween(
                startDate,
                endDate
        );
    }

    /*
     * Get number of orders between two dates.
     */
    @Transactional(readOnly = true)
    public long getOrdersBetween(
            LocalDateTime startDate,
            LocalDateTime endDate
    ) {

        return checkoutRepository.getOrdersBetween(
                startDate,
                endDate
        );
    }

    // =========================================================
    // INVOICE PDF GENERATION
    // =========================================================

    private byte[] generateInvoicePdf(
            CheckoutRequest request,
            List<CheckoutLine> lines,
            BigDecimal grandTotal,
            String invoiceNumber
    ) throws IOException {

        String invoiceDate =
                LocalDateTime.now()
                        .format(
                                DateTimeFormatter.ofPattern(
                                        "yyyy-MM-dd HH:mm"
                                )
                        );

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            PDPage page = new PDPage(PDRectangle.A4);

            document.addPage(page);

            try (
                    PDPageContentStream content =
                            new PDPageContentStream(
                                    document,
                                    page
                            )
            ) {

                float pageWidth =
                        PDRectangle.A4.getWidth();

                float y = 790;

                /*
                 * ==========================
                 * PAWCARE HEADER
                 * ==========================
                 */

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA_BOLD
                        ),
                        26
                );

                content.newLineAtOffset(
                        50,
                        y
                );

                content.showText("PAWCARE");

                content.endText();

                y -= 30;

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA
                        ),
                        11
                );

                content.newLineAtOffset(
                        50,
                        y
                );

                content.showText(
                        "Pet Care Management System"
                );

                content.endText();

                /*
                 * ==========================
                 * INVOICE TITLE
                 * ==========================
                 */

                y -= 60;

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA_BOLD
                        ),
                        22
                );

                content.newLineAtOffset(
                        50,
                        y
                );

                content.showText("INVOICE");

                content.endText();

                /*
                 * ==========================
                 * INVOICE DETAILS
                 * ==========================
                 */

                y -= 35;

                writeText(
                        content,
                        "Invoice Number: " + invoiceNumber,
                        50,
                        y,
                        11,
                        true
                );

                y -= 18;

                writeText(
                        content,
                        "Date: " + invoiceDate,
                        50,
                        y,
                        11,
                        false
                );

                /*
                 * ==========================
                 * CUSTOMER
                 * ==========================
                 */

                y -= 45;

                writeText(
                        content,
                        "CUSTOMER DETAILS",
                        50,
                        y,
                        13,
                        true
                );

                y -= 22;

                String customerName =
                        safeValue(
                                request.getCustomerName(),
                                "Customer"
                        );

                writeText(
                        content,
                        "Name: " + customerName,
                        50,
                        y,
                        11,
                        false
                );

                y -= 17;

                if (request.getCustomerEmail() != null &&
                        !request.getCustomerEmail().isBlank()) {

                    writeText(
                            content,
                            "Email: "
                                    + request.getCustomerEmail(),
                            50,
                            y,
                            11,
                            false
                    );

                    y -= 17;
                }

                if (request.getCustomerPhone() != null &&
                        !request.getCustomerPhone().isBlank()) {

                    writeText(
                            content,
                            "Phone: "
                                    + request.getCustomerPhone(),
                            50,
                            y,
                            11,
                            false
                    );

                    y -= 17;
                }

                if (request.getCustomerAddress() != null &&
                        !request.getCustomerAddress().isBlank()) {

                    writeText(
                            content,
                            "Address: "
                                    + request.getCustomerAddress(),
                            50,
                            y,
                            11,
                            false
                    );

                    y -= 17;
                }

                /*
                 * ==========================
                 * PRODUCT TABLE
                 * ==========================
                 */

                y -= 30;

                float xProduct = 50;
                float xQty = 330;
                float xPrice = 400;
                float xTotal = 480;

                writeText(
                        content,
                        "PRODUCT",
                        xProduct,
                        y,
                        11,
                        true
                );

                writeText(
                        content,
                        "QTY",
                        xQty,
                        y,
                        11,
                        true
                );

                writeText(
                        content,
                        "PRICE",
                        xPrice,
                        y,
                        11,
                        true
                );

                writeText(
                        content,
                        "TOTAL",
                        xTotal,
                        y,
                        11,
                        true
                );

                y -= 10;

                content.moveTo(
                        50,
                        y
                );

                content.lineTo(
                        pageWidth - 50,
                        y
                );

                content.stroke();

                y -= 22;

                /*
                 * ==========================
                 * PRODUCTS
                 * ==========================
                 */

                for (CheckoutLine line : lines) {

                    Product product = line.product();

                    String productName =
                            product.getProductName();

                    if (productName.length() > 32) {
                        productName =
                                productName.substring(0, 29)
                                        + "...";
                    }

                    writeText(
                            content,
                            productName,
                            xProduct,
                            y,
                            10,
                            false
                    );

                    writeText(
                            content,
                            String.valueOf(line.quantity()),
                            xQty,
                            y,
                            10,
                            false
                    );

                    writeText(
                            content,
                            "Rs. "
                                    + formatMoney(
                                    product.getPrice()
                            ),
                            xPrice,
                            y,
                            10,
                            false
                    );

                    writeText(
                            content,
                            "Rs. "
                                    + formatMoney(
                                    line.lineTotal()
                            ),
                            xTotal,
                            y,
                            10,
                            false
                    );

                    y -= 22;
                }

                /*
                 * ==========================
                 * TOTAL
                 * ==========================
                 */

                y -= 15;

                content.moveTo(
                        350,
                        y
                );

                content.lineTo(
                        pageWidth - 50,
                        y
                );

                content.stroke();

                y -= 30;

                writeText(
                        content,
                        "SUBTOTAL:",
                        370,
                        y,
                        11,
                        true
                );

                writeText(
                        content,
                        "Rs. "
                                + formatMoney(grandTotal),
                        480,
                        y,
                        11,
                        true
                );

                y -= 25;

                writeText(
                        content,
                        "DELIVERY:",
                        370,
                        y,
                        11,
                        false
                );

                writeText(
                        content,
                        "FREE",
                        480,
                        y,
                        11,
                        false
                );

                y -= 25;

                writeText(
                        content,
                        "TOTAL:",
                        370,
                        y,
                        14,
                        true
                );

                writeText(
                        content,
                        "Rs. "
                                + formatMoney(grandTotal),
                        480,
                        y,
                        14,
                        true
                );

                /*
                 * ==========================
                 * FOOTER
                 * ==========================
                 */

                y = 80;

                writeText(
                        content,
                        "Thank you for shopping with PawCare!",
                        50,
                        y,
                        11,
                        true
                );

                y -= 18;

                writeText(
                        content,
                        "Please keep this invoice for your records.",
                        50,
                        y,
                        10,
                        false
                );
            }

            document.save(outputStream);

            return outputStream.toByteArray();
        }
    }

    // =========================================================
    // PDF HELPER METHODS
    // =========================================================

    private void writeText(
            PDPageContentStream content,
            String text,
            float x,
            float y,
            float fontSize,
            boolean bold
    ) throws IOException {

        content.beginText();

        content.setFont(
                new PDType1Font(
                        bold
                                ? Standard14Fonts.FontName.HELVETICA_BOLD
                                : Standard14Fonts.FontName.HELVETICA
                ),
                fontSize
        );

        content.newLineAtOffset(
                x,
                y
        );

        content.showText(
                sanitizeText(text)
        );

        content.endText();
    }

    private String sanitizeText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("×", "x")
                .replace("–", "-")
                .replace("—", "-")
                .replace("’", "'")
                .replace("“", "\"")
                .replace("”", "\"");
    }

    private String safeValue(
            String value,
            String defaultValue
    ) {

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

    private String formatMoney(BigDecimal value) {

        return value
                .setScale(
                        2,
                        java.math.RoundingMode.HALF_UP
                )
                .toString();
    }

    // =========================================================
    // INTERNAL CHECKOUT LINE
    // =========================================================

    private record CheckoutLine(
            Product product,
            int quantity,
            BigDecimal lineTotal
    ) {
    }
}