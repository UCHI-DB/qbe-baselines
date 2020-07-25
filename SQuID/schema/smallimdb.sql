--
-- PostgreSQL database dump
--

-- Dumped from database version 9.6.6
-- Dumped by pg_dump version 10.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: plpgsql; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;


--
-- Name: EXTENSION plpgsql; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: _aggr_aoc_movietocertificate; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoc_movietocertificate (
    movie_id integer,
    certificate_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoc_movietocertificate OWNER TO afariha;

--
-- Name: _aggr_aoc_movietocountry; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoc_movietocountry (
    movie_id integer,
    country_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoc_movietocountry OWNER TO afariha;

--
-- Name: _aggr_aoc_movietogenre; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoc_movietogenre (
    movie_id integer,
    genre_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoc_movietogenre OWNER TO afariha;

--
-- Name: _aggr_aoc_movietolanguage; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoc_movietolanguage (
    movie_id integer,
    language_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoc_movietolanguage OWNER TO afariha;

--
-- Name: _aggr_aoo_castinfo_movie_idtoperson_id; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoo_castinfo_movie_idtoperson_id (
    movie_id integer,
    person_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoo_castinfo_movie_idtoperson_id OWNER TO afariha;

--
-- Name: _aggr_aoo_castinfo_person_idtomovie_id; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoo_castinfo_person_idtomovie_id (
    person_id integer,
    movie_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoo_castinfo_person_idtomovie_id OWNER TO afariha;

--
-- Name: _aggr_aoo_distribution_company_idtomovie_id; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoo_distribution_company_idtomovie_id (
    company_id integer,
    movie_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoo_distribution_company_idtomovie_id OWNER TO afariha;

--
-- Name: _aggr_aoo_distribution_movie_idtocompany_id; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoo_distribution_movie_idtocompany_id (
    movie_id integer,
    company_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoo_distribution_movie_idtocompany_id OWNER TO afariha;

--
-- Name: _aggr_aoo_production_company_idtomovie_id; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoo_production_company_idtomovie_id (
    company_id integer,
    movie_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoo_production_company_idtomovie_id OWNER TO afariha;

--
-- Name: _aggr_aoo_production_movie_idtocompany_id; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoo_production_movie_idtocompany_id (
    movie_id integer,
    company_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoo_production_movie_idtocompany_id OWNER TO afariha;

--
-- Name: _companytocertificate; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _companytocertificate (
    company_id integer NOT NULL,
    certificate_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _companytocertificate OWNER TO afariha;

--
-- Name: _companytocountry; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _companytocountry (
    company_id integer NOT NULL,
    country_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _companytocountry OWNER TO afariha;

--
-- Name: _companytogenre; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _companytogenre (
    company_id integer NOT NULL,
    genre_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _companytogenre OWNER TO afariha;

--
-- Name: _companytolanguage; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _companytolanguage (
    company_id integer NOT NULL,
    language_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _companytolanguage OWNER TO afariha;

--
-- Name: _companytoproduction_year; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _companytoproduction_year (
    company_id integer NOT NULL,
    production_year integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _companytoproduction_year OWNER TO afariha;

--
-- Name: _invertedcolumnindex; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _invertedcolumnindex (
    word text,
    tabname text,
    colname text
);


ALTER TABLE _invertedcolumnindex OWNER TO afariha;

--
-- Name: _movietoage_group; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _movietoage_group (
    movie_id integer NOT NULL,
    age_group text,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _movietoage_group OWNER TO afariha;

--
-- Name: _movietobirth_country; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _movietobirth_country (
    movie_id integer NOT NULL,
    birth_country text,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _movietobirth_country OWNER TO afariha;

--
-- Name: _movietobirth_year; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _movietobirth_year (
    movie_id integer NOT NULL,
    birth_year integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _movietobirth_year OWNER TO afariha;

--
-- Name: _movietodeath_cause; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _movietodeath_cause (
    movie_id integer NOT NULL,
    death_cause text,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _movietodeath_cause OWNER TO afariha;

--
-- Name: _movietodeath_year; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _movietodeath_year (
    movie_id integer NOT NULL,
    death_year integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _movietodeath_year OWNER TO afariha;

--
-- Name: _movietogender; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _movietogender (
    movie_id integer NOT NULL,
    gender text,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _movietogender OWNER TO afariha;

--
-- Name: _movietoheight_group; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _movietoheight_group (
    movie_id integer NOT NULL,
    height_group text,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _movietoheight_group OWNER TO afariha;

--
-- Name: _persontocastinfo_role_id; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _persontocastinfo_role_id (
    person_id integer NOT NULL,
    role_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _persontocastinfo_role_id OWNER TO afariha;

--
-- Name: _persontocertificate; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _persontocertificate (
    person_id integer NOT NULL,
    certificate_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _persontocertificate OWNER TO afariha;

--
-- Name: _persontocountry; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _persontocountry (
    person_id integer NOT NULL,
    country_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _persontocountry OWNER TO afariha;

--
-- Name: _persontogenre; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _persontogenre (
    person_id integer NOT NULL,
    genre_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _persontogenre OWNER TO afariha;

--
-- Name: _persontolanguage; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _persontolanguage (
    person_id integer NOT NULL,
    language_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _persontolanguage OWNER TO afariha;

--
-- Name: _persontoproduction_year; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _persontoproduction_year (
    person_id integer NOT NULL,
    production_year integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _persontoproduction_year OWNER TO afariha;

--
-- Name: castinfo; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE castinfo (
    movie_id integer,
    person_id integer,
    role_id integer
);


ALTER TABLE castinfo OWNER TO afariha;

--
-- Name: certificate; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE certificate (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE certificate OWNER TO afariha;

--
-- Name: company; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE company (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE company OWNER TO afariha;

--
-- Name: country; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE country (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE country OWNER TO afariha;

--
-- Name: distribution; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE distribution (
    movie_id integer,
    company_id integer
);


ALTER TABLE distribution OWNER TO afariha;

--
-- Name: genre; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE genre (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE genre OWNER TO afariha;

--
-- Name: language; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE language (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE language OWNER TO afariha;

--
-- Name: movie; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE movie (
    id integer NOT NULL,
    title text NOT NULL,
    production_year integer
);


ALTER TABLE movie OWNER TO afariha;

--
-- Name: movietocertificate; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE movietocertificate (
    movie_id integer,
    certificate_id integer
);


ALTER TABLE movietocertificate OWNER TO afariha;

--
-- Name: movietocountry; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE movietocountry (
    movie_id integer,
    country_id integer
);


ALTER TABLE movietocountry OWNER TO afariha;

--
-- Name: movietogenre; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE movietogenre (
    movie_id integer,
    genre_id integer
);


ALTER TABLE movietogenre OWNER TO afariha;

--
-- Name: movietolanguage; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE movietolanguage (
    movie_id integer,
    language_id integer
);


ALTER TABLE movietolanguage OWNER TO afariha;

--
-- Name: person; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE person (
    id integer NOT NULL,
    name text NOT NULL,
    gender text,
    birth_country text,
    birth_year integer,
    death_year integer,
    age_group text,
    death_cause text,
    height_group text
);


ALTER TABLE person OWNER TO afariha;

--
-- Name: production; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE production (
    movie_id integer,
    company_id integer
);


ALTER TABLE production OWNER TO afariha;

--
-- Name: role; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE role (
    id integer NOT NULL,
    role character varying(32) NOT NULL
);


ALTER TABLE role OWNER TO afariha;

--
-- Name: _invertedcolumnindex _invertedcolumnindex_word_tabname_colname_key; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _invertedcolumnindex
    ADD CONSTRAINT _invertedcolumnindex_word_tabname_colname_key UNIQUE (word, tabname, colname);


--
-- Name: certificate certificate_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY certificate
    ADD CONSTRAINT certificate_pkey PRIMARY KEY (id);


--
-- Name: company company_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY company
    ADD CONSTRAINT company_pkey PRIMARY KEY (id);

ALTER TABLE company CLUSTER ON company_pkey;


--
-- Name: country country_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_pkey PRIMARY KEY (id);


--
-- Name: genre genre_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY genre
    ADD CONSTRAINT genre_pkey PRIMARY KEY (id);


--
-- Name: language language_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY language
    ADD CONSTRAINT language_pkey PRIMARY KEY (id);


--
-- Name: movie movie_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY movie
    ADD CONSTRAINT movie_pkey PRIMARY KEY (id);

ALTER TABLE movie CLUSTER ON movie_pkey;


--
-- Name: person person_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY person
    ADD CONSTRAINT person_pkey PRIMARY KEY (id);

ALTER TABLE person CLUSTER ON person_pkey;


--
-- Name: role role_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY role
    ADD CONSTRAINT role_pkey PRIMARY KEY (id);


--
-- Name: _aggr_aoc_movietocertificate_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoc_movietocertificate_idx ON _aggr_aoc_movietocertificate USING btree (movie_id);


--
-- Name: _aggr_aoc_movietocountry_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoc_movietocountry_idx ON _aggr_aoc_movietocountry USING btree (movie_id);


--
-- Name: _aggr_aoc_movietogenre_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoc_movietogenre_idx ON _aggr_aoc_movietogenre USING btree (movie_id);


--
-- Name: _aggr_aoc_movietolanguage_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoc_movietolanguage_idx ON _aggr_aoc_movietolanguage USING btree (movie_id);


--
-- Name: _aggr_aoo_castinfo_movie_idtoperson_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoo_castinfo_movie_idtoperson_id_idx ON _aggr_aoo_castinfo_movie_idtoperson_id USING btree (movie_id);


--
-- Name: _aggr_aoo_castinfo_person_idtomovie_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoo_castinfo_person_idtomovie_id_idx ON _aggr_aoo_castinfo_person_idtomovie_id USING btree (person_id);


--
-- Name: _aggr_aoo_distribution_company_idtomovie_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoo_distribution_company_idtomovie_id_idx ON _aggr_aoo_distribution_company_idtomovie_id USING btree (company_id);


--
-- Name: _aggr_aoo_distribution_movie_idtocompany_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoo_distribution_movie_idtocompany_id_idx ON _aggr_aoo_distribution_movie_idtocompany_id USING btree (movie_id);


--
-- Name: _aggr_aoo_production_company_idtomovie_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoo_production_company_idtomovie_id_idx ON _aggr_aoo_production_company_idtomovie_id USING btree (company_id);


--
-- Name: _aggr_aoo_production_movie_idtocompany_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoo_production_movie_idtocompany_id_idx ON _aggr_aoo_production_movie_idtocompany_id USING btree (movie_id);


--
-- Name: _companytocertificate_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _companytocertificate_idx ON _companytocertificate USING btree (certificate_id, freq);

ALTER TABLE _companytocertificate CLUSTER ON _companytocertificate_idx;


--
-- Name: _companytocertificate_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _companytocertificate_idx_2 ON _companytocertificate USING btree (company_id);


--
-- Name: _companytocountry_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _companytocountry_idx ON _companytocountry USING btree (country_id, freq);

ALTER TABLE _companytocountry CLUSTER ON _companytocountry_idx;


--
-- Name: _companytocountry_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _companytocountry_idx_2 ON _companytocountry USING btree (company_id);


--
-- Name: _companytogenre_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _companytogenre_idx ON _companytogenre USING btree (genre_id, freq);

ALTER TABLE _companytogenre CLUSTER ON _companytogenre_idx;


--
-- Name: _companytogenre_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _companytogenre_idx_2 ON _companytogenre USING btree (company_id);


--
-- Name: _companytolanguage_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _companytolanguage_idx ON _companytolanguage USING btree (language_id, freq);

ALTER TABLE _companytolanguage CLUSTER ON _companytolanguage_idx;


--
-- Name: _companytolanguage_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _companytolanguage_idx_2 ON _companytolanguage USING btree (company_id);


--
-- Name: _companytoproduction_year_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _companytoproduction_year_idx ON _companytoproduction_year USING btree (production_year, freq);

ALTER TABLE _companytoproduction_year CLUSTER ON _companytoproduction_year_idx;


--
-- Name: _companytoproduction_year_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _companytoproduction_year_idx_2 ON _companytoproduction_year USING btree (company_id);


--
-- Name: _invertedcolumnindex_word_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _invertedcolumnindex_word_idx ON _invertedcolumnindex USING btree (word);

ALTER TABLE _invertedcolumnindex CLUSTER ON _invertedcolumnindex_word_idx;


--
-- Name: _movietoage_group_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietoage_group_idx ON _movietoage_group USING btree (age_group, freq);

ALTER TABLE _movietoage_group CLUSTER ON _movietoage_group_idx;


--
-- Name: _movietoage_group_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietoage_group_idx_2 ON _movietoage_group USING btree (movie_id);


--
-- Name: _movietobirth_country_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietobirth_country_idx ON _movietobirth_country USING btree (birth_country, freq);

ALTER TABLE _movietobirth_country CLUSTER ON _movietobirth_country_idx;


--
-- Name: _movietobirth_country_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietobirth_country_idx_2 ON _movietobirth_country USING btree (movie_id);


--
-- Name: _movietobirth_year_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietobirth_year_idx ON _movietobirth_year USING btree (birth_year, freq);

ALTER TABLE _movietobirth_year CLUSTER ON _movietobirth_year_idx;


--
-- Name: _movietobirth_year_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietobirth_year_idx_2 ON _movietobirth_year USING btree (movie_id);


--
-- Name: _movietodeath_cause_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietodeath_cause_idx ON _movietodeath_cause USING btree (death_cause, freq);

ALTER TABLE _movietodeath_cause CLUSTER ON _movietodeath_cause_idx;


--
-- Name: _movietodeath_cause_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietodeath_cause_idx_2 ON _movietodeath_cause USING btree (movie_id);


--
-- Name: _movietodeath_year_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietodeath_year_idx ON _movietodeath_year USING btree (death_year, freq);

ALTER TABLE _movietodeath_year CLUSTER ON _movietodeath_year_idx;


--
-- Name: _movietodeath_year_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietodeath_year_idx_2 ON _movietodeath_year USING btree (movie_id);


--
-- Name: _movietogender_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietogender_idx ON _movietogender USING btree (gender, freq);

ALTER TABLE _movietogender CLUSTER ON _movietogender_idx;


--
-- Name: _movietogender_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietogender_idx_2 ON _movietogender USING btree (movie_id);


--
-- Name: _movietoheight_group_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietoheight_group_idx ON _movietoheight_group USING btree (height_group, freq);

ALTER TABLE _movietoheight_group CLUSTER ON _movietoheight_group_idx;


--
-- Name: _movietoheight_group_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _movietoheight_group_idx_2 ON _movietoheight_group USING btree (movie_id);


--
-- Name: _persontocastinfo_role_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontocastinfo_role_id_idx ON _persontocastinfo_role_id USING btree (role_id, freq);

ALTER TABLE _persontocastinfo_role_id CLUSTER ON _persontocastinfo_role_id_idx;


--
-- Name: _persontocastinfo_role_id_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontocastinfo_role_id_idx_2 ON _persontocastinfo_role_id USING btree (person_id);


--
-- Name: _persontocertificate_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontocertificate_idx ON _persontocertificate USING btree (certificate_id, freq);

ALTER TABLE _persontocertificate CLUSTER ON _persontocertificate_idx;


--
-- Name: _persontocertificate_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontocertificate_idx_2 ON _persontocertificate USING btree (person_id);


--
-- Name: _persontocountry_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontocountry_idx ON _persontocountry USING btree (country_id, freq);

ALTER TABLE _persontocountry CLUSTER ON _persontocountry_idx;


--
-- Name: _persontocountry_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontocountry_idx_2 ON _persontocountry USING btree (person_id);


--
-- Name: _persontogenre_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontogenre_idx ON _persontogenre USING btree (genre_id, freq);

ALTER TABLE _persontogenre CLUSTER ON _persontogenre_idx;


--
-- Name: _persontogenre_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontogenre_idx_2 ON _persontogenre USING btree (person_id);


--
-- Name: _persontolanguage_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontolanguage_idx ON _persontolanguage USING btree (language_id, freq);

ALTER TABLE _persontolanguage CLUSTER ON _persontolanguage_idx;


--
-- Name: _persontolanguage_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontolanguage_idx_2 ON _persontolanguage USING btree (person_id);


--
-- Name: _persontoproduction_year_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontoproduction_year_idx ON _persontoproduction_year USING btree (production_year, freq);

ALTER TABLE _persontoproduction_year CLUSTER ON _persontoproduction_year_idx;


--
-- Name: _persontoproduction_year_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _persontoproduction_year_idx_2 ON _persontoproduction_year USING btree (person_id);


--
-- Name: castinfo_movie_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX castinfo_movie_id_idx ON castinfo USING btree (movie_id);

ALTER TABLE castinfo CLUSTER ON castinfo_movie_id_idx;


--
-- Name: castinfo_person_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX castinfo_person_id_idx ON castinfo USING btree (person_id);


--
-- Name: company_lower_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX company_lower_idx ON company USING btree (lower(name));


--
-- Name: company_name_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX company_name_idx ON company USING btree (name);


--
-- Name: distribution_company_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX distribution_company_id_idx ON distribution USING btree (company_id);


--
-- Name: distribution_movie_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX distribution_movie_id_idx ON distribution USING btree (movie_id);


--
-- Name: movie_lower_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX movie_lower_idx ON movie USING btree (lower(title));


--
-- Name: movie_title_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX movie_title_idx ON movie USING btree (title);


--
-- Name: person_lower_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX person_lower_idx ON person USING btree (lower(name));


--
-- Name: person_name_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX person_name_idx ON person USING btree (name);


--
-- Name: production_company_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX production_company_id_idx ON production USING btree (company_id);


--
-- Name: production_movie_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX production_movie_id_idx ON production USING btree (movie_id);


--
-- Name: _aggr_aoc_movietocertificate _aggr_aocmovietocertificate_movie_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoc_movietocertificate
    ADD CONSTRAINT _aggr_aocmovietocertificate_movie_id_fk FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _aggr_aoc_movietocountry _aggr_aocmovietocountry_movie_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoc_movietocountry
    ADD CONSTRAINT _aggr_aocmovietocountry_movie_id_fk FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _aggr_aoc_movietogenre _aggr_aocmovietogenre_movie_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoc_movietogenre
    ADD CONSTRAINT _aggr_aocmovietogenre_movie_id_fk FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _aggr_aoc_movietolanguage _aggr_aocmovietolanguage_movie_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoc_movietolanguage
    ADD CONSTRAINT _aggr_aocmovietolanguage_movie_id_fk FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _aggr_aoo_castinfo_movie_idtoperson_id _aggr_aoo_castinfo_movie_idtoperson_id_movie_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoo_castinfo_movie_idtoperson_id
    ADD CONSTRAINT _aggr_aoo_castinfo_movie_idtoperson_id_movie_id_fk FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _aggr_aoo_castinfo_person_idtomovie_id _aggr_aoo_castinfo_person_idtomovie_id_person_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoo_castinfo_person_idtomovie_id
    ADD CONSTRAINT _aggr_aoo_castinfo_person_idtomovie_id_person_id_fk FOREIGN KEY (person_id) REFERENCES person(id);


--
-- Name: _aggr_aoo_distribution_company_idtomovie_id _aggr_aoo_distribution_company_idtomovie_id_company_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoo_distribution_company_idtomovie_id
    ADD CONSTRAINT _aggr_aoo_distribution_company_idtomovie_id_company_id_fk FOREIGN KEY (company_id) REFERENCES company(id);


--
-- Name: _aggr_aoo_distribution_movie_idtocompany_id _aggr_aoo_distribution_movie_idtocompany_id_movie_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoo_distribution_movie_idtocompany_id
    ADD CONSTRAINT _aggr_aoo_distribution_movie_idtocompany_id_movie_id_fk FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _aggr_aoo_production_company_idtomovie_id _aggr_aoo_production_company_idtomovie_id_company_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoo_production_company_idtomovie_id
    ADD CONSTRAINT _aggr_aoo_production_company_idtomovie_id_company_id_fk FOREIGN KEY (company_id) REFERENCES company(id);


--
-- Name: _aggr_aoo_production_movie_idtocompany_id _aggr_aoo_production_movie_idtocompany_id_movie_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoo_production_movie_idtocompany_id
    ADD CONSTRAINT _aggr_aoo_production_movie_idtocompany_id_movie_id_fk FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _companytocertificate _companytocertificate_certificate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _companytocertificate
    ADD CONSTRAINT _companytocertificate_certificate_id_fkey FOREIGN KEY (certificate_id) REFERENCES certificate(id);


--
-- Name: _companytocertificate _companytocertificate_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _companytocertificate
    ADD CONSTRAINT _companytocertificate_company_id_fkey FOREIGN KEY (company_id) REFERENCES company(id);


--
-- Name: _companytocountry _companytocountry_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _companytocountry
    ADD CONSTRAINT _companytocountry_company_id_fkey FOREIGN KEY (company_id) REFERENCES company(id);


--
-- Name: _companytocountry _companytocountry_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _companytocountry
    ADD CONSTRAINT _companytocountry_country_id_fkey FOREIGN KEY (country_id) REFERENCES country(id);


--
-- Name: _companytogenre _companytogenre_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _companytogenre
    ADD CONSTRAINT _companytogenre_company_id_fkey FOREIGN KEY (company_id) REFERENCES company(id);


--
-- Name: _companytogenre _companytogenre_genre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _companytogenre
    ADD CONSTRAINT _companytogenre_genre_id_fkey FOREIGN KEY (genre_id) REFERENCES genre(id);


--
-- Name: _companytolanguage _companytolanguage_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _companytolanguage
    ADD CONSTRAINT _companytolanguage_company_id_fkey FOREIGN KEY (company_id) REFERENCES company(id);


--
-- Name: _companytolanguage _companytolanguage_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _companytolanguage
    ADD CONSTRAINT _companytolanguage_language_id_fkey FOREIGN KEY (language_id) REFERENCES language(id);


--
-- Name: _companytoproduction_year _companytoproduction_year_company_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _companytoproduction_year
    ADD CONSTRAINT _companytoproduction_year_company_id_fkey FOREIGN KEY (company_id) REFERENCES company(id);


--
-- Name: _movietoage_group _movietoage_group_movie_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _movietoage_group
    ADD CONSTRAINT _movietoage_group_movie_id_fkey FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _movietobirth_country _movietobirth_country_movie_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _movietobirth_country
    ADD CONSTRAINT _movietobirth_country_movie_id_fkey FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _movietobirth_year _movietobirth_year_movie_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _movietobirth_year
    ADD CONSTRAINT _movietobirth_year_movie_id_fkey FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _movietodeath_cause _movietodeath_cause_movie_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _movietodeath_cause
    ADD CONSTRAINT _movietodeath_cause_movie_id_fkey FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _movietodeath_year _movietodeath_year_movie_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _movietodeath_year
    ADD CONSTRAINT _movietodeath_year_movie_id_fkey FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _movietogender _movietogender_movie_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _movietogender
    ADD CONSTRAINT _movietogender_movie_id_fkey FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _movietoheight_group _movietoheight_group_movie_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _movietoheight_group
    ADD CONSTRAINT _movietoheight_group_movie_id_fkey FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: _persontocastinfo_role_id _persontocastinfo_role_id_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _persontocastinfo_role_id
    ADD CONSTRAINT _persontocastinfo_role_id_person_id_fkey FOREIGN KEY (person_id) REFERENCES person(id);


--
-- Name: _persontocastinfo_role_id _persontocastinfo_role_id_role_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _persontocastinfo_role_id
    ADD CONSTRAINT _persontocastinfo_role_id_role_id_fkey FOREIGN KEY (role_id) REFERENCES role(id);


--
-- Name: _persontocertificate _persontocertificate_certificate_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _persontocertificate
    ADD CONSTRAINT _persontocertificate_certificate_id_fkey FOREIGN KEY (certificate_id) REFERENCES certificate(id);


--
-- Name: _persontocertificate _persontocertificate_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _persontocertificate
    ADD CONSTRAINT _persontocertificate_person_id_fkey FOREIGN KEY (person_id) REFERENCES person(id);


--
-- Name: _persontocountry _persontocountry_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _persontocountry
    ADD CONSTRAINT _persontocountry_country_id_fkey FOREIGN KEY (country_id) REFERENCES country(id);


--
-- Name: _persontocountry _persontocountry_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _persontocountry
    ADD CONSTRAINT _persontocountry_person_id_fkey FOREIGN KEY (person_id) REFERENCES person(id);


--
-- Name: _persontogenre _persontogenre_genre_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _persontogenre
    ADD CONSTRAINT _persontogenre_genre_id_fkey FOREIGN KEY (genre_id) REFERENCES genre(id);


--
-- Name: _persontogenre _persontogenre_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _persontogenre
    ADD CONSTRAINT _persontogenre_person_id_fkey FOREIGN KEY (person_id) REFERENCES person(id);


--
-- Name: _persontolanguage _persontolanguage_language_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _persontolanguage
    ADD CONSTRAINT _persontolanguage_language_id_fkey FOREIGN KEY (language_id) REFERENCES language(id);


--
-- Name: _persontolanguage _persontolanguage_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _persontolanguage
    ADD CONSTRAINT _persontolanguage_person_id_fkey FOREIGN KEY (person_id) REFERENCES person(id);


--
-- Name: _persontoproduction_year _persontoproduction_year_person_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _persontoproduction_year
    ADD CONSTRAINT _persontoproduction_year_person_id_fkey FOREIGN KEY (person_id) REFERENCES person(id);


--
-- Name: castinfo fk1; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY castinfo
    ADD CONSTRAINT fk1 FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: movietogenre fk10; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY movietogenre
    ADD CONSTRAINT fk10 FOREIGN KEY (genre_id) REFERENCES genre(id);


--
-- Name: movietolanguage fk10; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY movietolanguage
    ADD CONSTRAINT fk10 FOREIGN KEY (language_id) REFERENCES language(id);


--
-- Name: movietogenre fk11; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY movietogenre
    ADD CONSTRAINT fk11 FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: movietolanguage fk11; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY movietolanguage
    ADD CONSTRAINT fk11 FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: production fk12; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY production
    ADD CONSTRAINT fk12 FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: production fk13; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY production
    ADD CONSTRAINT fk13 FOREIGN KEY (company_id) REFERENCES company(id);


--
-- Name: castinfo fk2; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY castinfo
    ADD CONSTRAINT fk2 FOREIGN KEY (person_id) REFERENCES person(id);


--
-- Name: castinfo fk3; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY castinfo
    ADD CONSTRAINT fk3 FOREIGN KEY (role_id) REFERENCES role(id);


--
-- Name: distribution fk4; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY distribution
    ADD CONSTRAINT fk4 FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: distribution fk5; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY distribution
    ADD CONSTRAINT fk5 FOREIGN KEY (company_id) REFERENCES company(id);


--
-- Name: movietocertificate fk6; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY movietocertificate
    ADD CONSTRAINT fk6 FOREIGN KEY (certificate_id) REFERENCES certificate(id);


--
-- Name: movietocertificate fk7; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY movietocertificate
    ADD CONSTRAINT fk7 FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- Name: movietocountry fk8; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY movietocountry
    ADD CONSTRAINT fk8 FOREIGN KEY (country_id) REFERENCES country(id);


--
-- Name: movietocountry fk9; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY movietocountry
    ADD CONSTRAINT fk9 FOREIGN KEY (movie_id) REFERENCES movie(id);


--
-- PostgreSQL database dump complete
--

