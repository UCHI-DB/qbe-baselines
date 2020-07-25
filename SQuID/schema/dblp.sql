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


--
-- Name: fuzzystrmatch; Type: EXTENSION; Schema: -; Owner: 
--

CREATE EXTENSION IF NOT EXISTS fuzzystrmatch WITH SCHEMA public;


--
-- Name: EXTENSION fuzzystrmatch; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION fuzzystrmatch IS 'determine similarities and distance between strings';


SET search_path = public, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- Name: _aggr_aoc_authortocountry; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoc_authortocountry (
    author_id integer,
    country_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoc_authortocountry OWNER TO afariha;

--
-- Name: _aggr_aoc_authortoinstitute; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoc_authortoinstitute (
    author_id integer,
    institute_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoc_authortoinstitute OWNER TO afariha;

--
-- Name: _aggr_aoc_publicationtocountry; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoc_publicationtocountry (
    publication_id integer,
    country_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoc_publicationtocountry OWNER TO afariha;

--
-- Name: _aggr_aoc_publicationtodomain; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoc_publicationtodomain (
    publication_id integer,
    domain_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoc_publicationtodomain OWNER TO afariha;

--
-- Name: _aggr_aoc_publicationtoinstitute; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoc_publicationtoinstitute (
    publication_id integer,
    institute_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoc_publicationtoinstitute OWNER TO afariha;

--
-- Name: _aggr_aoc_publicationtovenue; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoc_publicationtovenue (
    publication_id integer,
    venue_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoc_publicationtovenue OWNER TO afariha;

--
-- Name: _aggr_aoo_authortopublication_author_idtopublication_id; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoo_authortopublication_author_idtopublication_id (
    author_id integer,
    publication_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoo_authortopublication_author_idtopublication_id OWNER TO afariha;

--
-- Name: _aggr_aoo_authortopublication_publication_idtoauthor_id; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _aggr_aoo_authortopublication_publication_idtoauthor_id (
    publication_id integer,
    author_id_aggr integer[],
    count bigint
);


ALTER TABLE _aggr_aoo_authortopublication_publication_idtoauthor_id OWNER TO afariha;

--
-- Name: _authortoauthortopublication_position_id; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _authortoauthortopublication_position_id (
    author_id integer NOT NULL,
    position_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _authortoauthortopublication_position_id OWNER TO afariha;

--
-- Name: _authortocountry; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _authortocountry (
    author_id integer NOT NULL,
    country_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _authortocountry OWNER TO afariha;

--
-- Name: _authortodomain; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _authortodomain (
    author_id integer NOT NULL,
    domain_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _authortodomain OWNER TO afariha;

--
-- Name: _authortoinstitute; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _authortoinstitute (
    author_id integer NOT NULL,
    institute_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _authortoinstitute OWNER TO afariha;

--
-- Name: _authortovenue; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _authortovenue (
    author_id integer NOT NULL,
    venue_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _authortovenue OWNER TO afariha;

--
-- Name: _authortoyear; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _authortoyear (
    author_id integer NOT NULL,
    year integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _authortoyear OWNER TO afariha;

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
-- Name: _publicationtocountry; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _publicationtocountry (
    publication_id integer NOT NULL,
    country_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _publicationtocountry OWNER TO afariha;

--
-- Name: _publicationtogender; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _publicationtogender (
    publication_id integer NOT NULL,
    gender text,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _publicationtogender OWNER TO afariha;

--
-- Name: _publicationtoinstitute; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _publicationtoinstitute (
    publication_id integer NOT NULL,
    institute_id integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _publicationtoinstitute OWNER TO afariha;

--
-- Name: _publicationtopaper_count; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE _publicationtopaper_count (
    publication_id integer NOT NULL,
    paper_count integer,
    freq integer,
    normalized_freq integer
);


ALTER TABLE _publicationtopaper_count OWNER TO afariha;

--
-- Name: author; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE author (
    id integer NOT NULL,
    paper_count integer,
    name text NOT NULL,
    gender text
);


ALTER TABLE author OWNER TO afariha;

--
-- Name: authorposition; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE authorposition (
    id integer NOT NULL,
    name text
);


ALTER TABLE authorposition OWNER TO afariha;

--
-- Name: authortocountry; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE authortocountry (
    author_id integer,
    country_id integer
);


ALTER TABLE authortocountry OWNER TO afariha;

--
-- Name: authortoinstitute; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE authortoinstitute (
    author_id integer,
    institute_id integer
);


ALTER TABLE authortoinstitute OWNER TO afariha;

--
-- Name: authortopublication; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE authortopublication (
    publication_id integer,
    author_id integer,
    position_id integer
);


ALTER TABLE authortopublication OWNER TO afariha;

--
-- Name: country; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE country (
    id integer NOT NULL,
    name text
);


ALTER TABLE country OWNER TO afariha;

--
-- Name: domain; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE domain (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE domain OWNER TO afariha;

--
-- Name: institute; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE institute (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE institute OWNER TO afariha;

--
-- Name: publication; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE publication (
    id integer NOT NULL,
    name text NOT NULL,
    year integer
);


ALTER TABLE publication OWNER TO afariha;

--
-- Name: publicationtocountry; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE publicationtocountry (
    publication_id integer,
    country_id integer
);


ALTER TABLE publicationtocountry OWNER TO afariha;

--
-- Name: publicationtodomain; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE publicationtodomain (
    publication_id integer,
    domain_id integer
);


ALTER TABLE publicationtodomain OWNER TO afariha;

--
-- Name: publicationtoinstitute; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE publicationtoinstitute (
    publication_id integer,
    institute_id integer
);


ALTER TABLE publicationtoinstitute OWNER TO afariha;

--
-- Name: publicationtovenue; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE publicationtovenue (
    publication_id integer,
    venue_id integer
);


ALTER TABLE publicationtovenue OWNER TO afariha;

--
-- Name: venue; Type: TABLE; Schema: public; Owner: afariha
--

CREATE TABLE venue (
    id integer NOT NULL,
    name text NOT NULL
);


ALTER TABLE venue OWNER TO afariha;

--
-- Name: _invertedcolumnindex _invertedcolumnindex_word_tabname_colname_key; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _invertedcolumnindex
    ADD CONSTRAINT _invertedcolumnindex_word_tabname_colname_key UNIQUE (word, tabname, colname);


--
-- Name: author author_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY author
    ADD CONSTRAINT author_pkey PRIMARY KEY (id);


--
-- Name: authorposition authorposition_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY authorposition
    ADD CONSTRAINT authorposition_pkey PRIMARY KEY (id);


--
-- Name: country country_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY country
    ADD CONSTRAINT country_pkey PRIMARY KEY (id);


--
-- Name: institute institute_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY institute
    ADD CONSTRAINT institute_pkey PRIMARY KEY (id);


--
-- Name: publicationtocountry key_const; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY publicationtocountry
    ADD CONSTRAINT key_const UNIQUE (publication_id, country_id);


--
-- Name: publicationtoinstitute key_const2; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY publicationtoinstitute
    ADD CONSTRAINT key_const2 UNIQUE (publication_id, institute_id);


--
-- Name: publication publication_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY publication
    ADD CONSTRAINT publication_pkey PRIMARY KEY (id);


--
-- Name: domain type_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY domain
    ADD CONSTRAINT type_pkey PRIMARY KEY (id);


--
-- Name: venue venue_pkey; Type: CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY venue
    ADD CONSTRAINT venue_pkey PRIMARY KEY (id);


--
-- Name: _aggr_aoc_authortocountry_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoc_authortocountry_idx ON _aggr_aoc_authortocountry USING btree (author_id);


--
-- Name: _aggr_aoc_authortoinstitute_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoc_authortoinstitute_idx ON _aggr_aoc_authortoinstitute USING btree (author_id);


--
-- Name: _aggr_aoc_publicationtocountry_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoc_publicationtocountry_idx ON _aggr_aoc_publicationtocountry USING btree (publication_id);


--
-- Name: _aggr_aoc_publicationtodomain_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoc_publicationtodomain_idx ON _aggr_aoc_publicationtodomain USING btree (publication_id);


--
-- Name: _aggr_aoc_publicationtoinstitute_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoc_publicationtoinstitute_idx ON _aggr_aoc_publicationtoinstitute USING btree (publication_id);


--
-- Name: _aggr_aoc_publicationtovenue_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoc_publicationtovenue_idx ON _aggr_aoc_publicationtovenue USING btree (publication_id);


--
-- Name: _aggr_aoo_authortopublication_author_idtopublication_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoo_authortopublication_author_idtopublication_id_idx ON _aggr_aoo_authortopublication_author_idtopublication_id USING btree (author_id);


--
-- Name: _aggr_aoo_authortopublication_publication_idtoauthor_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _aggr_aoo_authortopublication_publication_idtoauthor_id_idx ON _aggr_aoo_authortopublication_publication_idtoauthor_id USING btree (publication_id);


--
-- Name: _authortoauthortopublication_position_id_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortoauthortopublication_position_id_idx ON _authortoauthortopublication_position_id USING btree (position_id, freq);

ALTER TABLE _authortoauthortopublication_position_id CLUSTER ON _authortoauthortopublication_position_id_idx;


--
-- Name: _authortoauthortopublication_position_id_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortoauthortopublication_position_id_idx_2 ON _authortoauthortopublication_position_id USING btree (author_id);


--
-- Name: _authortocountry_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortocountry_idx ON _authortocountry USING btree (country_id, freq);

ALTER TABLE _authortocountry CLUSTER ON _authortocountry_idx;


--
-- Name: _authortocountry_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortocountry_idx_2 ON _authortocountry USING btree (author_id);


--
-- Name: _authortodomain_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortodomain_idx ON _authortodomain USING btree (domain_id, freq);

ALTER TABLE _authortodomain CLUSTER ON _authortodomain_idx;


--
-- Name: _authortodomain_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortodomain_idx_2 ON _authortodomain USING btree (author_id);


--
-- Name: _authortoinstitute_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortoinstitute_idx ON _authortoinstitute USING btree (institute_id, freq);

ALTER TABLE _authortoinstitute CLUSTER ON _authortoinstitute_idx;


--
-- Name: _authortoinstitute_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortoinstitute_idx_2 ON _authortoinstitute USING btree (author_id);


--
-- Name: _authortovenue_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortovenue_idx ON _authortovenue USING btree (venue_id, freq);

ALTER TABLE _authortovenue CLUSTER ON _authortovenue_idx;


--
-- Name: _authortovenue_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortovenue_idx_2 ON _authortovenue USING btree (author_id);


--
-- Name: _authortoyear_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortoyear_idx ON _authortoyear USING btree (year, freq);

ALTER TABLE _authortoyear CLUSTER ON _authortoyear_idx;


--
-- Name: _authortoyear_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _authortoyear_idx_2 ON _authortoyear USING btree (author_id);


--
-- Name: _invertedcolumnindex_word_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _invertedcolumnindex_word_idx ON _invertedcolumnindex USING btree (word);

ALTER TABLE _invertedcolumnindex CLUSTER ON _invertedcolumnindex_word_idx;


--
-- Name: _publicationtocountry_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _publicationtocountry_idx ON _publicationtocountry USING btree (country_id, freq);

ALTER TABLE _publicationtocountry CLUSTER ON _publicationtocountry_idx;


--
-- Name: _publicationtocountry_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _publicationtocountry_idx_2 ON _publicationtocountry USING btree (publication_id);


--
-- Name: _publicationtogender_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _publicationtogender_idx ON _publicationtogender USING btree (gender, freq);

ALTER TABLE _publicationtogender CLUSTER ON _publicationtogender_idx;


--
-- Name: _publicationtogender_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _publicationtogender_idx_2 ON _publicationtogender USING btree (publication_id);


--
-- Name: _publicationtoinstitute_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _publicationtoinstitute_idx ON _publicationtoinstitute USING btree (institute_id, freq);

ALTER TABLE _publicationtoinstitute CLUSTER ON _publicationtoinstitute_idx;


--
-- Name: _publicationtoinstitute_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _publicationtoinstitute_idx_2 ON _publicationtoinstitute USING btree (publication_id);


--
-- Name: _publicationtopaper_count_idx; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _publicationtopaper_count_idx ON _publicationtopaper_count USING btree (paper_count, freq);

ALTER TABLE _publicationtopaper_count CLUSTER ON _publicationtopaper_count_idx;


--
-- Name: _publicationtopaper_count_idx_2; Type: INDEX; Schema: public; Owner: afariha
--

CREATE INDEX _publicationtopaper_count_idx_2 ON _publicationtopaper_count USING btree (publication_id);


--
-- Name: _aggr_aoc_authortocountry _aggr_aocauthortocountry_author_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoc_authortocountry
    ADD CONSTRAINT _aggr_aocauthortocountry_author_id_fk FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: _aggr_aoc_authortoinstitute _aggr_aocauthortoinstitute_author_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoc_authortoinstitute
    ADD CONSTRAINT _aggr_aocauthortoinstitute_author_id_fk FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: _aggr_aoc_publicationtocountry _aggr_aocpublicationtocountry_publication_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoc_publicationtocountry
    ADD CONSTRAINT _aggr_aocpublicationtocountry_publication_id_fk FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: _aggr_aoc_publicationtodomain _aggr_aocpublicationtodomain_publication_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoc_publicationtodomain
    ADD CONSTRAINT _aggr_aocpublicationtodomain_publication_id_fk FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: _aggr_aoc_publicationtoinstitute _aggr_aocpublicationtoinstitute_publication_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoc_publicationtoinstitute
    ADD CONSTRAINT _aggr_aocpublicationtoinstitute_publication_id_fk FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: _aggr_aoc_publicationtovenue _aggr_aocpublicationtovenue_publication_id_fk; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoc_publicationtovenue
    ADD CONSTRAINT _aggr_aocpublicationtovenue_publication_id_fk FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: _aggr_aoo_authortopublication_author_idtopublication_id _aggr_aoo_authortopublication_author_idtopublication_id_author_; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoo_authortopublication_author_idtopublication_id
    ADD CONSTRAINT _aggr_aoo_authortopublication_author_idtopublication_id_author_ FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: _aggr_aoo_authortopublication_publication_idtoauthor_id _aggr_aoo_authortopublication_publication_idtoauthor_id_publica; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _aggr_aoo_authortopublication_publication_idtoauthor_id
    ADD CONSTRAINT _aggr_aoo_authortopublication_publication_idtoauthor_id_publica FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: _authortoauthortopublication_position_id _authortoauthortopublication_position_id_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _authortoauthortopublication_position_id
    ADD CONSTRAINT _authortoauthortopublication_position_id_author_id_fkey FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: _authortoauthortopublication_position_id _authortoauthortopublication_position_id_position_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _authortoauthortopublication_position_id
    ADD CONSTRAINT _authortoauthortopublication_position_id_position_id_fkey FOREIGN KEY (position_id) REFERENCES authorposition(id);


--
-- Name: _authortocountry _authortocountry_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _authortocountry
    ADD CONSTRAINT _authortocountry_author_id_fkey FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: _authortocountry _authortocountry_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _authortocountry
    ADD CONSTRAINT _authortocountry_country_id_fkey FOREIGN KEY (country_id) REFERENCES country(id);


--
-- Name: _authortodomain _authortodomain_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _authortodomain
    ADD CONSTRAINT _authortodomain_author_id_fkey FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: _authortodomain _authortodomain_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _authortodomain
    ADD CONSTRAINT _authortodomain_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id);


--
-- Name: _authortoinstitute _authortoinstitute_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _authortoinstitute
    ADD CONSTRAINT _authortoinstitute_author_id_fkey FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: _authortoinstitute _authortoinstitute_institute_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _authortoinstitute
    ADD CONSTRAINT _authortoinstitute_institute_id_fkey FOREIGN KEY (institute_id) REFERENCES institute(id);


--
-- Name: _authortovenue _authortovenue_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _authortovenue
    ADD CONSTRAINT _authortovenue_author_id_fkey FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: _authortovenue _authortovenue_venue_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _authortovenue
    ADD CONSTRAINT _authortovenue_venue_id_fkey FOREIGN KEY (venue_id) REFERENCES venue(id);


--
-- Name: _authortoyear _authortoyear_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _authortoyear
    ADD CONSTRAINT _authortoyear_author_id_fkey FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: _publicationtocountry _publicationtocountry_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _publicationtocountry
    ADD CONSTRAINT _publicationtocountry_country_id_fkey FOREIGN KEY (country_id) REFERENCES country(id);


--
-- Name: _publicationtocountry _publicationtocountry_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _publicationtocountry
    ADD CONSTRAINT _publicationtocountry_publication_id_fkey FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: _publicationtogender _publicationtogender_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _publicationtogender
    ADD CONSTRAINT _publicationtogender_publication_id_fkey FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: _publicationtoinstitute _publicationtoinstitute_institute_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _publicationtoinstitute
    ADD CONSTRAINT _publicationtoinstitute_institute_id_fkey FOREIGN KEY (institute_id) REFERENCES institute(id);


--
-- Name: _publicationtoinstitute _publicationtoinstitute_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _publicationtoinstitute
    ADD CONSTRAINT _publicationtoinstitute_publication_id_fkey FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: _publicationtopaper_count _publicationtopaper_count_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY _publicationtopaper_count
    ADD CONSTRAINT _publicationtopaper_count_publication_id_fkey FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: authortocountry authortocountry_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY authortocountry
    ADD CONSTRAINT authortocountry_author_id_fkey FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: authortocountry authortocountry_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY authortocountry
    ADD CONSTRAINT authortocountry_country_id_fkey FOREIGN KEY (country_id) REFERENCES country(id);


--
-- Name: authortoinstitute authortoinstitute_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY authortoinstitute
    ADD CONSTRAINT authortoinstitute_author_id_fkey FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: authortoinstitute authortoinstitute_institute_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY authortoinstitute
    ADD CONSTRAINT authortoinstitute_institute_id_fkey FOREIGN KEY (institute_id) REFERENCES institute(id);


--
-- Name: authortopublication authortopublication_author_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY authortopublication
    ADD CONSTRAINT authortopublication_author_id_fkey FOREIGN KEY (author_id) REFERENCES author(id);


--
-- Name: authortopublication authortopublication_position_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY authortopublication
    ADD CONSTRAINT authortopublication_position_id_fkey FOREIGN KEY (position_id) REFERENCES authorposition(id);


--
-- Name: authortopublication authortopublication_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY authortopublication
    ADD CONSTRAINT authortopublication_publication_id_fkey FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: publicationtocountry publicationtocountry_country_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY publicationtocountry
    ADD CONSTRAINT publicationtocountry_country_id_fkey FOREIGN KEY (country_id) REFERENCES country(id);


--
-- Name: publicationtocountry publicationtocountry_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY publicationtocountry
    ADD CONSTRAINT publicationtocountry_publication_id_fkey FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: publicationtodomain publicationtodomain_domain_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY publicationtodomain
    ADD CONSTRAINT publicationtodomain_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(id);


--
-- Name: publicationtodomain publicationtodomain_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY publicationtodomain
    ADD CONSTRAINT publicationtodomain_publication_id_fkey FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: publicationtoinstitute publicationtoinstitute_institute_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY publicationtoinstitute
    ADD CONSTRAINT publicationtoinstitute_institute_id_fkey FOREIGN KEY (institute_id) REFERENCES institute(id);


--
-- Name: publicationtoinstitute publicationtoinstitute_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY publicationtoinstitute
    ADD CONSTRAINT publicationtoinstitute_publication_id_fkey FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: publicationtovenue publicationtovenue_publication_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY publicationtovenue
    ADD CONSTRAINT publicationtovenue_publication_id_fkey FOREIGN KEY (publication_id) REFERENCES publication(id);


--
-- Name: publicationtovenue publicationtovenue_venue_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: afariha
--

ALTER TABLE ONLY publicationtovenue
    ADD CONSTRAINT publicationtovenue_venue_id_fkey FOREIGN KEY (venue_id) REFERENCES venue(id);


--
-- PostgreSQL database dump complete
--

