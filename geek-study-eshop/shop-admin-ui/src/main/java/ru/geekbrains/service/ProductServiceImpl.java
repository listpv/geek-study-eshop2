package ru.geekbrains.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.geekbrains.controller.repr.ProductRepr;
import ru.geekbrains.error.NotFoundException;
import ru.geekbrains.persist.model.Picture;
import ru.geekbrains.persist.model.PictureData;
import ru.geekbrains.persist.model.Product;
import ru.geekbrains.persist.repo.ProductRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService, Serializable {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;

    private final PictureService pictureService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, PictureService pictureService) {
        this.productRepository = productRepository;
        this.pictureService = pictureService;
    }

    @Override
    @Transactional
    public List<ProductRepr> findAll() {
        return productRepository.findAll().stream()
                .map(ProductRepr::new)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Optional<ProductRepr> findById(Long id) {
        return productRepository.findById(id).map(ProductRepr::new);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void save(ProductRepr productRepr) throws IOException {
        Product product = (productRepr.getId() != null) ? productRepository.findById(productRepr.getId())
                .orElseThrow(NotFoundException::new) : new Product();
        product.setName(productRepr.getName());
        product.setCategory(productRepr.getCategory());
        product.setBrand(productRepr.getBrand());
        product.setPrice(productRepr.getPrice());

//        if (productRepr.getNewPictures() != null) {
//            for (MultipartFile newPicture : productRepr.getNewPictures()) {
//                logger.info("Product {} file {} size {} contentType {}", productRepr.getId(),
//                        newPicture.getOriginalFilename(), newPicture.getSize(), newPicture.getContentType());
//
//                if (product.getPictures() == null) {
//                    product.setPictures(new ArrayList<>());
//                }
//
//                product.getPictures().add(new Picture(
//                        newPicture.getOriginalFilename(),
//                        newPicture.getContentType(),
//                        pictureService.createPictureData(newPicture.getBytes()),
//                        product
//                ));
//            }
//        }

        if (productRepr.getNewPictures() != null) {
            for (MultipartFile newPicture : productRepr.getNewPictures()) {
                logger.info("Product {} file {} size {} contentType {}", productRepr.getId(),
                        newPicture.getOriginalFilename(), newPicture.getSize(), newPicture.getContentType());


                String filename = String.valueOf(java.util.UUID.randomUUID());

                File newfile = new File("ProductImage/" + product.getName());
                newfile.mkdir();

                try (OutputStream outFile = new BufferedOutputStream(new FileOutputStream( newfile + "/" + filename)))

                {
                    for(int i = 0; i < newPicture.getSize(); i++)
                    {
                        outFile.write(newPicture.getInputStream().read());

                    }
                }



                if (product.getPictures() == null) {
                    product.setPictures(new ArrayList<>());
                }

                product.getPictures().add(new Picture(
                        newPicture.getOriginalFilename(),
                        newPicture.getContentType(),
                        pictureService.createPictureData(newfile + "/" + filename),
                        product
                ));
            }
        }

        productRepository.save(product);
    }
}
