package ru.geekbrains.service;

import ru.geekbrains.persist.model.PictureData;

import java.util.Optional;

public interface PictureService {

    Optional<String> getPictureContentTypeById(long id);

    Optional<byte[]> getPictureDataById(long id);

    PictureData createPictureData(byte[] picture);

    PictureData createPictureData(String file);

    Optional<String> getPictureDataFilesById(long id);

}
