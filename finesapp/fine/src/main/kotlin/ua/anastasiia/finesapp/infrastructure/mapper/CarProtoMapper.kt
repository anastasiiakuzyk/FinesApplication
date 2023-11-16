package ua.anastasiia.finesapp.infrastructure.mapper

import ua.anastasiia.finesapp.domain.Fine
import ua.anastasiia.finesapp.commonmodels.car.Car as ProtoCar

fun Fine.Car.toProto(): ProtoCar =
    ProtoCar.newBuilder()
        .setPlate(plate)
        .setMake(make)
        .setModel(model)
        .setColor(color)
        .build()

fun ProtoCar.toCar() = Fine.Car(
    plate = plate,
    make = make,
    model = model,
    color = color
)
