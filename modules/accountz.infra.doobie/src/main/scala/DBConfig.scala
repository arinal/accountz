package io.lamedh.accountz.infra.repo.doobie

final case class DBConfig(
    url: String,
    driver: String,
    user: String,
    password: String
)
