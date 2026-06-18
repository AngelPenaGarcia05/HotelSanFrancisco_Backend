package com.sanfrancisco.api.modules.recepcion.service.interfaces;

import com.sanfrancisco.api.modules.recepcion.dto.request.ReservaOnlineRequest;
import com.sanfrancisco.api.modules.recepcion.dto.response.ReservaResponse;

public interface ReservaOnlineService {

    ReservaResponse crear(ReservaOnlineRequest request);
}
