package org.unicef.rapidreg.service.impl;


import androidx.core.util.Pair;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.raizlabs.android.dbflow.data.Blob;

import org.unicef.rapidreg.PrimeroAppConfiguration;
import org.unicef.rapidreg.base.record.recordphoto.PhotoConfig;
import org.unicef.rapidreg.exception.ObservableNullResponseException;
import org.unicef.rapidreg.model.RecordModel;
import org.unicef.rapidreg.model.TracingPhoto;
import org.unicef.rapidreg.repository.TracingPhotoDao;
import org.unicef.rapidreg.repository.remote.SyncTracingsRepository;
import org.unicef.rapidreg.service.BaseRetrofitService;
import org.unicef.rapidreg.service.SyncTracingService;
import org.unicef.rapidreg.service.cache.ItemValuesMap;

import java.util.List;

import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import io.reactivex.Observable;

public class SyncTracingServiceImpl extends BaseRetrofitService<SyncTracingsRepository> implements SyncTracingService {
    private TracingPhotoDao tracingPhotoDao;

    public SyncTracingServiceImpl(TracingPhotoDao tracingPhotoDao) {
        this.tracingPhotoDao = tracingPhotoDao;
    }

    @Override
    protected String getBaseUrl() {
        return PrimeroAppConfiguration.getApiBaseUrl();
    }

    public Observable<Response<ResponseBody>> getPhoto(String id, String photoKey, String
            photoSize) {
        return getRepository(SyncTracingsRepository.class).getPhoto(PrimeroAppConfiguration.getCookie(), id, photoKey,
                photoSize);
    }

    public Observable<Response<ResponseBody>> getAudio(String id) {
        return getRepository(SyncTracingsRepository.class).getAudio(PrimeroAppConfiguration.getCookie(), id);
    }

    public Observable<Response<JsonElement>> get(String id, String locale, Boolean isMobile) {
        return getRepository(SyncTracingsRepository.class).get(PrimeroAppConfiguration.getCookie(), id, locale,
                isMobile);
    }

    public Observable<Response<JsonElement>> getIds(String lastUpdate, Boolean isMobile) {
        return getRepository(SyncTracingsRepository.class).getIds(PrimeroAppConfiguration.getCookie(), lastUpdate,
                isMobile);
    }

    public Response<JsonElement> uploadJsonProfile(RecordModel item) throws ObservableNullResponseException {
        ItemValuesMap values = ItemValuesMap.fromJson(new String(item.getContent().getBlob()));
        String shortUUID = org.unicef.rapidreg.utils.TextUtils.getLastSevenNumbers(item.getUniqueId());

        values.addStringItem("short_id", shortUUID);
        values.addStringItem("_id", item.getInternalId());
        values.addStringItem("unique_identifier", item.getUniqueIdentifier());
        values.removeItem("_attachments");

        JsonObject jsonObject = new JsonObject();
        jsonObject.add("tracing_request", new Gson().fromJson(new Gson().toJson(values.getValues
                ()), JsonObject.class));

        Observable<Response<JsonElement>> responseObservable;
        if (!TextUtils.isEmpty(item.getInternalId()) && item.getLastSyncedDate() != null) {
            responseObservable = getRepository(SyncTracingsRepository.class).put(PrimeroAppConfiguration.getCookie(),
                    item.getInternalId(),
                    jsonObject);
        } else {
            responseObservable = getRepository(SyncTracingsRepository.class).postExcludeMediaData
                    (PrimeroAppConfiguration
                    .getCookie(), jsonObject);
        }
        Response<JsonElement> response = responseObservable.blockingFirst();
        if (!response.isSuccessful()) {
            throw new ObservableNullResponseException();
        }

        JsonObject responseJsonObject = response.body().getAsJsonObject();

        item.setInternalId(responseJsonObject.get("_id").getAsString());
        item.setInternalRev(responseJsonObject.get("_rev").getAsString());
        responseJsonObject.remove("histories");
        item.setContent(new Blob(responseJsonObject.toString().getBytes()));
        item.update();

        return response;
    }

    public void uploadAudio(RecordModel item) throws ObservableNullResponseException {
        if (item.getAudio() != null) {
            RequestBody requestFile = RequestBody.create(MediaType.parse(
                    PhotoConfig.CONTENT_TYPE_AUDIO), item.getAudio().getBlob());
            MultipartBody.Part body = MultipartBody.Part.createFormData(FORM_DATA_KEY_AUDIO,
                    "audioFile.amr", requestFile);
            Observable<Response<JsonElement>> observable = getRepository(SyncTracingsRepository.class).postMediaData(
                    PrimeroAppConfiguration.getCookie(), item.getInternalId(), body);

            Response<JsonElement> response = observable.blockingFirst();

            verifyResponse(response);

            item.setAudioSynced(true);
            updateRecordRev(item, response.body().getAsJsonObject().get("_rev").getAsString());
        }
    }

    public Call<Response<JsonElement>> deletePhotos(String id, JsonArray photoKeys) {
        JsonObject requestBody = new JsonObject();
        JsonObject requestPhotoKeys = new JsonObject();
        for (JsonElement photoKey : photoKeys) {
            requestPhotoKeys.addProperty(photoKey.getAsString(), 1);
        }
        requestBody.add("delete_tracing_request_photo", requestPhotoKeys);
        return getRepository(SyncTracingsRepository.class).deletePhoto(PrimeroAppConfiguration.getCookie(), id,
                requestBody);
    }

    public void uploadPhotos(final RecordModel record) {
        List<Long> tracingPhotos = tracingPhotoDao.getIdsByTracingId(record.getId());
        Observable.fromIterable(tracingPhotos)
                .filter(tracingPhotoId -> true)
                .flatMap(new Function<Long, Observable<Pair<TracingPhoto, Response<JsonElement>>>>() {
                    @Override
                    public Observable<Pair<TracingPhoto, Response<JsonElement>>> apply(final Long
                                                                                              tracingPhotoId) {
                        return Observable.create(new ObservableOnSubscribe<Pair<TracingPhoto, Response<JsonElement>>>() {
                            @Override
                            public void subscribe(ObservableEmitter<Pair<TracingPhoto, Response<JsonElement>>> emitter) throws ObservableNullResponseException {
                                TracingPhoto tracingPhoto = tracingPhotoDao.getById
                                        (tracingPhotoId);

                                RequestBody requestFile = RequestBody.create(MediaType.parse
                                                (PhotoConfig.CONTENT_TYPE_IMAGE),
                                        tracingPhoto.getPhoto().getBlob());
                                MultipartBody.Part body = MultipartBody.Part.createFormData
                                        (FORM_DATA_KEY_PHOTO, tracingPhoto.getKey() + ".jpg",
                                                requestFile);
                                Observable<Response<JsonElement>> observable = getRepository(SyncTracingsRepository
                                        .class)
                                        .postMediaData(PrimeroAppConfiguration.getCookie(), record
                                                .getInternalId(), body);
                                Response<JsonElement> response = observable.blockingFirst();
                                verifyResponse(response);
                                emitter.onNext(new Pair<>(tracingPhoto, response));
                                emitter.onComplete();
                            }
                        });
                    }
                })
                .map(tracingPhotoResponsePair -> {
                    Response<JsonElement> response = tracingPhotoResponsePair.second;
                    TracingPhoto tracingPhoto = tracingPhotoResponsePair.first;
                    updateRecordRev(record, response.body().getAsJsonObject().get("_rev")
                            .getAsString());
                    updateTracingPhotoSyncStatus(tracingPhoto, true);
                    return tracingPhotoResponsePair;
                }).toList().blockingGet();
    }

    private void updateRecordRev(RecordModel record, String revId) {
        record.setInternalRev(revId);
        record.update();
    }

    private void updateTracingPhotoSyncStatus(TracingPhoto tracingPhoto, boolean status) {
        tracingPhoto.setSynced(status);
        tracingPhoto.update();
    }

    private static final String FORM_DATA_KEY_AUDIO = "tracing_request[audio]";

    private static final String FORM_DATA_KEY_PHOTO = "tracing_request[photo][0]";

    private void verifyResponse(Response<JsonElement> response) throws ObservableNullResponseException {
        if (!response.isSuccessful()) {
            throw new ObservableNullResponseException();
        }
    }
}


