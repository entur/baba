CREATE TABLE m2m_client
(
    pk                    bigint                 NOT NULL,
    entity_version        bigint                 NOT NULL,
    lock_version          bigint                 NOT NULL,
    private_code          character varying(255) NOT NULL,
    name                  character varying(255) NOT NULL,
    entur_organisation_id bigint                 NOT NULL,
    issuer                character varying(10)  NOT NULL
);


ALTER TABLE m2m_client OWNER TO baba;

ALTER TABLE m2m_client
    ADD CONSTRAINT m2m_client_pkey PRIMARY KEY (pk);

ALTER TABLE m2m_client
    ADD CONSTRAINT m2m_client_unique_private_code UNIQUE (private_code, entity_version);


CREATE TABLE m2m_client_responsibility_sets
(
    m2mclient_pk           bigint NOT NULL,
    responsibility_sets_pk bigint NOT NULL
);

ALTER TABLE m2m_client_responsibility_sets OWNER TO baba;

ALTER TABLE m2m_client_responsibility_sets
    ADD CONSTRAINT m2m_client_responsibility_sets_pkey PRIMARY KEY (m2mclient_pk, responsibility_sets_pk);

ALTER TABLE m2m_client_responsibility_sets
    ADD CONSTRAINT m2m_client_responsibility_sets_fk_m2m_client FOREIGN KEY (m2mclient_pk) REFERENCES m2m_client (pk);

ALTER TABLE m2m_client_responsibility_sets
    ADD CONSTRAINT m2m_client_responsibility_sets_fk_responsibility_set FOREIGN KEY (responsibility_sets_pk) REFERENCES responsibility_set (pk);

