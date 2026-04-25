package com.david.ProyectoFinal.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QrService {

    /// genera el QR como imagen PNG en bytes
    public byte[] generarQrComoPng(String contenido, int ancho, int alto) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    contenido,
                    BarcodeFormat.QR_CODE,
                    ancho,
                    alto,
                    hints
            );

            BufferedImage imagen = convertirBitMatrixAImagen(bitMatrix);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(imagen, "PNG", outputStream);

            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("No se pudo generar el código QR", e);
        }
    }

    /// genera el QR en base64 por si quieres insertarlo en html
    public String generarQrComoBase64(String contenido, int ancho, int alto) {
        byte[] qrBytes = generarQrComoPng(contenido, ancho, alto);
        return Base64.getEncoder().encodeToString(qrBytes);
    }

    private BufferedImage convertirBitMatrixAImagen(BitMatrix bitMatrix) {
        int ancho = bitMatrix.getWidth();
        int alto = bitMatrix.getHeight();

        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < ancho; x++) {
            for (int y = 0; y < alto; y++) {
                imagen.setRGB(x, y, bitMatrix.get(x, y) ? 0x000000 : 0xFFFFFF);
            }
        }

        return imagen;
    }
}